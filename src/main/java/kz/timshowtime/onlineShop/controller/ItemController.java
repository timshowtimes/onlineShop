package kz.timshowtime.onlineShop.controller;

import jakarta.servlet.http.HttpServletResponse;
import kz.timshowtime.onlineShop.enums.SortName;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.ItemPageable;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.service.CartItemService;
import kz.timshowtime.onlineShop.service.CartService;
import kz.timshowtime.onlineShop.service.ItemService;
import kz.timshowtime.onlineShop.specification.ItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;
    private final CartItemService cartItemService;

    @Value("${server.port}")
    private String port;

    @GetMapping
    public String all(@RequestParam(value = "search", required = false) String name,
                      @RequestParam(value = "sort", required = false, defaultValue = "NO") SortName sortValue,
                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                      @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                      Model model) {

        Sort sort = sortValue.getFieldName().equals("no") ? Sort.unsorted() : Sort.by(sortValue.getFieldName().toLowerCase());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        ItemPageable paging = ItemPageable.getPageable(pageNumber, pageSize, itemService.count());
        List<Item> items = itemService.findAll(ItemSpecification.nameOrDescContains(name), pageable);
        List<Item> quantities = cartItemService.getAllItems();
        Map<Long, Integer> quantityMap = quantities.stream()
                .collect(Collectors.toMap(Item::getId, Item::getQuantity));

        model.addAttribute("items", items);
        model.addAttribute("sort", sortValue.name());
        model.addAttribute("search", name);
        model.addAttribute("paging", paging);
        model.addAttribute("quantities", quantityMap);
        model.addAttribute("totalQuantity", cartItemService.getTotalQuantity());
        model.addAttribute("port", port);

        return "main";
    }

    @GetMapping("/images/{id}")
    public void getPostImage(@PathVariable("id") Long postId, HttpServletResponse response) throws IOException {
        byte[] image = itemService.getImageByPostId(postId);

        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLength(image.length);
        response.getOutputStream().write(image);
        response.getOutputStream().flush();
    }

    @GetMapping("/{id}")
    public String getOne(@PathVariable("id") Long id,
                         Model model) {
        int quantity = cartItemService.findQuantityById(id);
        model.addAttribute("item", itemService.findById(id));
        model.addAttribute("quantity", quantity);
        return "item";
    }

    @PostMapping("/{id}")
    public String putOnCart(@PathVariable("id") Long itemId,
                            @RequestParam(value = "action", defaultValue = "plus") String action,
                            @RequestParam(value = "source") String source) {
        Cart cart = cartService.findById(1);
        Item item = itemService.findById(itemId);
        int entityQuantity = cartItemService.findQuantityById(itemId);

        if (entityQuantity == 0 && action.equals("minus")) {
            return String.format("redirect:/%s", source);
        }

        switch (action) {
            case "plus" -> {
                entityQuantity += 1;
                cart.setTotalPrice(cart.getTotalPrice() + item.getPrice());
            }
            case "minus" -> {
                entityQuantity -= 1;
                cart.setTotalPrice(cart.getTotalPrice() - item.getPrice());
                if (entityQuantity == 0) {
                    cartItemService.deleteByItem(item);
                    return redirect(source);
                }
            }
            case "delete" -> {
                cart.setTotalPrice(cart.getTotalPrice() - (item.getPrice() * entityQuantity));
                cartItemService.deleteByItem(item);
                return redirect(source);
            }
        }

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .item(item)
                .quantity(entityQuantity)
                .createDt(LocalDateTime.now())
                .build();

        cartItemService.save(cartItem);
        cartService.save(cart);
        return redirect(source);
    }

    private String redirect(String source) {
        return "redirect:/" + source;
    }

    @GetMapping("/add-page")
    public String addPage() {
        return "item-add";
    }

    @PostMapping("/upload")
    public String addItem(@RequestParam("image") MultipartFile image,
                          @RequestParam("name") String name,
                          @RequestParam("price") Integer price,
                          @RequestParam("text") String text) throws IOException {

        Item item = Item.builder()
                .preview(image.getBytes())
                .name(name)
                .price(price)
                .description(text)
                .build();

        itemService.save(item);

        return "redirect:/items";
    }
}
