package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.service.OrderItemService;
import kz.timshowtime.onlineShop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderControllerReactive {
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @GetMapping
    public Mono<String> all(Model model) {
        return orderItemService.findAllOrdersWithItems()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("{id}")
    public Mono<String> order(@PathVariable("id") int id,
                              @RequestParam(name = "new", defaultValue = "false") boolean newOrder,
                              Model model) {
        return orderService.findById(id)
                .flatMap(order -> orderService.getAllItemsByOrder(order.getId())
                        .collectList()
                        .map(items -> {
                            model.addAttribute("order", order);
                            model.addAttribute("newOrder", newOrder);
                            model.addAttribute("items", items);
                            return "order";
                        }));
    }
}
