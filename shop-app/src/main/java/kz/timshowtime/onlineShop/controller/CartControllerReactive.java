package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.factory.WalletsApiFactory;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
import kz.timshowtime.onlineShop.paymentsclient.model.BalanceResponse;
import kz.timshowtime.onlineShop.paymentsclient.model.ChargeRequest;
import kz.timshowtime.onlineShop.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/cart")
public class CartControllerReactive {

    private final CartItemService cartItemService;
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final WalletsApiFactory walletsApiFactory;

    @GetMapping("/items")
    public Mono<String> getItems(Model model,
                                 @RequestParam(name = "cache", defaultValue = "true") Boolean cache,
                                 @RequestParam(name = "evictById", defaultValue = "0") Long evictById) {
        Flux<ItemDto> itemFlux = cartItemService.getAllItems(cache, evictById);
        Mono<Cart> cartMono = cartService.findById(1);
        Mono<BalanceResponse> balanceResponseMono = new WalletsApi().getBalance()
                .onErrorResume(ex -> {
                    BalanceResponse fallback = new BalanceResponse()
                            .balance(-1.0)
                            .currency(null);
                    return Mono.just(fallback);
                });

        Mono<List<ItemDto>> itemListMono = itemFlux.collectList();

        return Mono.zip(itemListMono, cartMono, balanceResponseMono)
                .map(tuple -> {
                    List<ItemDto> items = tuple.getT1();
                    Cart cart = tuple.getT2();
                    BalanceResponse balanceResponse = tuple.getT3();
                    String readableBalance = balanceResponse.getBalance() >= 0
                            ? String.format("%,.0f %s", balanceResponse.getBalance(), balanceResponse.getCurrency())
                            : "0,(Недоступно)"
                            .replace(',', ' ');

                    Map<Long, Integer> quantityMap = items.stream()
                            .collect(Collectors.toMap(ItemDto::getId, ItemDto::getQuantity));

                    String readableTotal = String.format("%,d ₸", cart.getTotalPrice()).replace(',', ' ');
                    int total = cart.getTotalPrice();

                    model.addAttribute("items", items);
                    model.addAttribute("quantities", quantityMap);
                    model.addAttribute("readableTotal", readableTotal);
                    model.addAttribute("readableBalance", readableBalance);
                    model.addAttribute("total", total);
                    model.addAttribute("balance", balanceResponse.getBalance());

                    return "cart";
                });

    }

    @PostMapping("/{itemId}")
    public Mono<String> putOnCart(@PathVariable Long itemId,
                                  ServerWebExchange exchange) {

        return exchange.getFormData().flatMap(form -> {

            String action = Optional.ofNullable(form.getFirst("action"))
                    .orElse("plus");
            String source = form.getFirst("source");

            Mono<Cart> cartMono = cartService.findById(1L);
            Mono<ItemDto> itemMono = itemService.findById(itemId);
            Mono<Integer> qtyMono = cartItemService
                    .findQuantityByItemId(itemId)   // текущее кол‑во
                    .defaultIfEmpty(0);

            return Mono.zip(cartMono, itemMono, qtyMono)
                    .flatMap(tuple -> {

                        Cart cart = tuple.getT1();
                        ItemDto item = tuple.getT2();
                        int quantity = tuple.getT3();

                        if ("minus".equals(action) && quantity == 0) {
                            return Mono.just("redirect:/" + source + "?cache=false");
                        }

                        switch (action) {
                            case "plus" -> {
                                quantity++;
                                cart.setTotalPrice(cart.getTotalPrice() + item.getPrice());
                            }
                            case "minus" -> {
                                quantity--;
                                cart.setTotalPrice(cart.getTotalPrice() - item.getPrice());
                            }
                            case "delete" -> {
                                cart.setTotalPrice(cart.getTotalPrice() - item.getPrice() * quantity);
                                quantity = 0;
                            }
                        }

                        Mono<Void> persist;
                        if (quantity == 0) {
                            persist = cartItemService.deleteByItemId(itemId)
                                    .then(cartService.save(cart))
                                    .then();
                        } else {
                            CartItem cartItem = CartItem.builder()
                                    .cartId(cart.getId())
                                    .itemId(itemId)
                                    .quantity(quantity)
                                    .createDt(LocalDateTime.now())
                                    .build();

                            persist = cartItemService.saveOrUpdate(cartItem)
                                    .then(cartService.save(cart))
                                    .then();
                        }

                        return persist.thenReturn("redirect:/" + source
                                + "?" + (quantity == 0 ? "evictById=" + itemId : "cache=false"));
                    });
        });
    }


    @PostMapping("/buy")
    public Mono<String> buy() {
        Mono<Cart> cartMono = cartService.findById(1);
        Mono<Long> countMono = orderService.count();
        Mono<Long> nextOrderIdMono = orderService.getNextOrderId();

        return Mono.zip(cartMono, countMono, nextOrderIdMono)
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    Long count = tuple.getT2();
                    Long nextOrderId = tuple.getT3();
                    double totalPrice = cart.getTotalPrice();

                    return cartItemService.getCartItems(cart.getId())
                            .collectList()
                            .flatMap(cartItems -> {
                                Map<Long, Integer> itemsIdWithQuantities = cartItems.stream()
                                        .collect(Collectors.toMap(CartItem::getItemId, CartItem::getQuantity));

                                String orderName = "Заказ №" + System.currentTimeMillis() + (count + 1);

                                WalletsApi walletsApi = walletsApiFactory.create();
                                ChargeRequest chargeRequest = new ChargeRequest();
                                chargeRequest.setAmount(totalPrice);
                                chargeRequest.setOrderId(BigDecimal.valueOf(nextOrderId));

                                return walletsApi.charge(chargeRequest)
                                        .doOnSuccess(result -> log.debug("Списание успешно, результат: {}", result))
                                        .flatMap(chargeResult ->
                                                createAndSaveOrder(orderName, itemsIdWithQuantities, cart.getTotalPrice())
                                                        .doOnSuccess(order -> log.debug("Заказ сохранен, id заказа: {}", order.getId()))
                                                        .flatMap(savedOrder ->
                                                                cartItemService.deleteAllByCart(cart)
                                                                        .doOnSuccess(v -> log.debug("Корзина очищена."))
                                                                        .then(Mono.defer(() -> {
                                                                            cart.setTotalPrice(0);
                                                                            return cartService.save(cart);
                                                                        }))
                                                                        .doOnSuccess(v -> log.debug("Общая сумма корзины обнулена."))
                                                                        .thenReturn("redirect:/orders/" + savedOrder.getId() + "?new=true")
                                                        ))
                                        .onErrorResume(ex -> Mono.error(new RuntimeException("Ошибка при списании: " + ex.getMessage(), ex)));

                            });
                });

    }

    public Mono<Order> createAndSaveOrder(String name, Map<Long, Integer> itemIdQuantityMap, int total) {
        Order orderToSave = Order.builder()
                .name(name)
                .totalPrice(total)
                .createDt(LocalDateTime.now())
                .build();

        return orderService.save(orderToSave)
                .flatMap(savedOrder -> {
                    List<OrdersItem> items = itemIdQuantityMap.entrySet().stream()
                            .map(entry -> OrdersItem.builder()
                                    .orderId(savedOrder.getId())
                                    .itemId(entry.getKey())
                                    .quantity(entry.getValue())
                                    .build())
                            .toList();

                    return orderItemService.saveAll(items).thenReturn(savedOrder);
                });

    }

}
