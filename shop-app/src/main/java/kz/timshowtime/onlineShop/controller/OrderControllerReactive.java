package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.security.UserDetailsImpl;
import kz.timshowtime.onlineShop.service.CartItemService;
import kz.timshowtime.onlineShop.service.OrderItemService;
import kz.timshowtime.onlineShop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import static kz.timshowtime.onlineShop.service.CartItemService.CART_ITEMS_KEY;

@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderControllerReactive {
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final CartItemService cartItemService;

    @GetMapping
    public Mono<String> all(Model model,
                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return orderItemService.findAllOrdersWithItems(userDetails.getId())
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("{id}")
    public Mono<String> order(@PathVariable("id") int id,
                              @RequestParam(name = "new", defaultValue = "false") boolean newOrder,
                              @AuthenticationPrincipal UserDetailsImpl userDetails,
                              Model model) {

        Long userId = userDetails.getId();

        return cartItemService.evictCartItemById(-1L, userId)
                .doOnSuccess(evict -> log.info("🗑️ [Корзина] Удаляем все товары из кэша {}", CART_ITEMS_KEY))
                .then(orderService.findById(id)
                        .flatMap(order -> orderService.getAllItemsByOrder(order.getId())
                                .collectList()
                                .map(items -> {
                                    model.addAttribute("order", order);
                                    model.addAttribute("newOrder", newOrder);
                                    model.addAttribute("items", items);
                                    return "order";
                                }))
                );
    }
}
