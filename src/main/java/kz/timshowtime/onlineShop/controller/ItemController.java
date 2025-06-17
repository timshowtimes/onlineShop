package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{id}")
    public String getOne(@PathVariable("id") Long id,
                         Model model) {
        model.addAttribute("item", itemService.getById(id));
        return "item";
    }

    @GetMapping("/add-page")
    public String addPage() {
        return "item-add";
    }
}
