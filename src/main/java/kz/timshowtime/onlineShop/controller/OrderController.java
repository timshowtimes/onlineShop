package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import kz.timshowtime.onlineShop.model.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    public final OrderService orderService;

    @GetMapping
    public String all(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("{id}")
    public String order(@PathVariable("id") int id,
                        @RequestParam(name = "new", defaultValue = "false") boolean newOrder,
                        Model model) {
        Order order = orderService.findById(id);
        List<Item> items = orderService.getAllItemsByOrder(order.getId());
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        model.addAttribute("items", items);
        return "order";
    }
}
