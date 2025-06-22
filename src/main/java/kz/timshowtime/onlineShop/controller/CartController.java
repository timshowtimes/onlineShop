package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import kz.timshowtime.onlineShop.service.CartItemService;
import kz.timshowtime.onlineShop.service.CartService;
import kz.timshowtime.onlineShop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartItemService cartItemService;
    private final CartService cartService;
    public final OrderService orderService;

    @GetMapping("/items")
    public String getItems(Model model) {
        List<Item> items = cartItemService.getAllItems();
        Map<Long, Integer> quantityMap = items.stream()
                .collect(Collectors.toMap(Item::getId, Item::getQuantity));
        long total = cartService.findById(1).getTotalPrice();
        model.addAttribute("items", items);
        model.addAttribute("quantities", quantityMap);
        model.addAttribute("total", String.format("%,d тг.", total).replace(',', ' '));

        return "cart";
    }

    @PostMapping("/buy")
    public String buy() {
        Cart cart = cartService.findById(1);
        int total = cartService.findById(1).getTotalPrice();
        long count = orderService.count();

        Map<Item, Integer> itemsWithQuantities = new HashMap<>();
        for (CartItem cartItem : cartItemService.getCartItems(cart)) {
            itemsWithQuantities.put(cartItem.getItem(), cartItem.getQuantity());
        }

        Order order = createOrder("Заказ №" + System.currentTimeMillis() + (count + 1),
                itemsWithQuantities, total);
        cartItemService.deleteAllByCart(cart);
//        cartService.deleteAllById(cart.getId());
        cart.setTotalPrice(0);
        cartService.save(cart);
        return "redirect:/orders/" + order.getId() + "?new=true";
    }

    public Order createOrder(String name, Map<Item, Integer> itemQuantityMap, int total) {
        Order order = Order.builder()
                .name(name)
                .totalPrice(total)
                .createDt(LocalDateTime.now())
                .build();

        List<OrdersItem> orderItemList = new ArrayList<>();

        for (Map.Entry<Item, Integer> iq : itemQuantityMap.entrySet()) {
            Item item = iq.getKey();
            int quantity = iq.getValue();

            OrdersItem ordersItem = new OrdersItem();
            ordersItem.setOrder(order);
            ordersItem.setItem(item);
            ordersItem.setQuantity(quantity);

            orderItemList.add(ordersItem);
        }

        order.setItems(orderItemList);
        order.setTotalPrice(total);

        return orderService.save(order);
    }
}
