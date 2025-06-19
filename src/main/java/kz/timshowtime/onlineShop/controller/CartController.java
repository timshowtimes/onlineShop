package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.dto.ItemQuantityDto;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.service.CartItemService;
import kz.timshowtime.onlineShop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartItemService cartItemService;
    private final CartService cartService;

    @GetMapping("/items")
    public String getItems(Model model) {
        List<Item> items = cartItemService.getAllItems();
        List<ItemQuantityDto> quantities = cartItemService.getItemQuantities(1);
        Map<Long, Integer> quantityMap = quantities.stream()
                .collect(Collectors.toMap(ItemQuantityDto::itemId, ItemQuantityDto::quantity));
        long total = cartService.findById(1).getTotalPrice();
        model.addAttribute("items", items);
        model.addAttribute("quantities", quantityMap);
        model.addAttribute("total", total);

        return "cart";
    }
}
