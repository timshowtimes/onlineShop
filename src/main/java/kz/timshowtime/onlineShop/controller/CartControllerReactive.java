package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import kz.timshowtime.onlineShop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartControllerReactive {

    private final CartItemService cartItemService;
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @GetMapping("/items")
    public Mono<String> getItems(Model model) {
        Flux<ItemDto> itemFlux = cartItemService.getAllItems();
        Mono<Cart> cartMono = cartService.findById(1);

        Mono<List<ItemDto>> itemListMono = itemFlux.collectList();

        return Mono.zip(itemListMono, cartMono)
                .map(tuple -> {
                    List<ItemDto> items = tuple.getT1();
                    Cart cart = tuple.getT2();

                    Map<Long, Integer> quantityMap = items.stream()
                            .collect(Collectors.toMap(ItemDto::getId, ItemDto::getQuantity));

                    String total = String.format("%,d тг", cart.getTotalPrice()).replace(',', ' ');


                    model.addAttribute("items", items);
                    model.addAttribute("quantities", quantityMap);
                    model.addAttribute("total", total);

                    return "cart"; // имя HTML-шаблона
                });

    }

    @PostMapping("/{itemId}")
    public Mono<String> putOnCart(@PathVariable Long itemId,
                                  ServerWebExchange exchange) {

        return exchange.getFormData().flatMap(form -> {

            String action = Optional.ofNullable(form.getFirst("action"))
                    .orElse("plus");
            String source = form.getFirst("source");

            Mono<Cart>   cartMono = cartService.findById(1L);
            Mono<Item>   itemMono = itemService.findById(itemId);
            Mono<Integer>qtyMono  = cartItemService
                    .findQuantityByItemId(itemId)   // текущее кол‑во
                    .defaultIfEmpty(0);

            return Mono.zip(cartMono, itemMono, qtyMono)
                    .flatMap(tuple -> {

                        Cart cart       = tuple.getT1();
                        Item item       = tuple.getT2();
                        int  quantity   = tuple.getT3();

                        if ("minus".equals(action) && quantity == 0) {
                            return Mono.just("redirect:/" + source);
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

                        return persist.thenReturn("redirect:/" + source);
                    });
        });
    }


    @PostMapping("/buy")
    public Mono<String> buy() {
        Mono<Cart> cartMono = cartService.findById(1);
        Mono<Long> countMono = orderService.count();

        return Mono.zip(cartMono, countMono)
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    Long count = tuple.getT2();

                    return cartItemService.getCartItems(cart.getId())
                            .collectList()
                            .flatMap(cartItems -> {
                                Map<Long, Integer> itemsIdWithQuantities = cartItems.stream()
                                        .collect(Collectors.toMap(CartItem::getItemId, CartItem::getQuantity));

                                String orderName = "Заказ №" + System.currentTimeMillis() + (count + 1);

                                return createAndSaveOrder(orderName, itemsIdWithQuantities, cart.getTotalPrice())
                                        .flatMap(savedOrder ->
                                                cartItemService.deleteAllByCart(cart)
                                                        .then(Mono.defer(() -> {
                                                            cart.setTotalPrice(0);
                                                            return cartService.save(cart);
                                                        }))
                                                        .thenReturn("redirect:/orders/" + savedOrder.getId() + "?new=true")
                                        );

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
