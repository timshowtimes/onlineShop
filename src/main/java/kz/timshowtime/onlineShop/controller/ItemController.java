package kz.timshowtime.onlineShop.controller;

import jakarta.servlet.http.HttpServletResponse;
import kz.timshowtime.onlineShop.dto.ItemQuantityDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping
    public String all(@RequestParam(value = "search", required = false) String name,
                      @RequestParam(value = "sort", required = false, defaultValue = "NO") SortName sortValue,
                      @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                      @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                      Model model) {

        Sort sort = sortValue.getFieldName().equals("no") ? Sort.unsorted() : Sort.by(sortValue.getFieldName().toLowerCase());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        ItemPageable paging = ItemPageable.getPageable(pageNumber, pageSize, itemService.count());
        List<Item> items = itemService.findAll(ItemSpecification.nameOrDescContains(name), pageable);
        List<ItemQuantityDto> quantities = cartItemService.getItemQuantities(1);
        Map<Long, Integer> quantityMap = quantities.stream()
                .collect(Collectors.toMap(ItemQuantityDto::itemId, ItemQuantityDto::quantity));

        model.addAttribute("items", items);
        model.addAttribute("sort", sortValue.name());
        model.addAttribute("search", name);
        model.addAttribute("paging", paging);
        model.addAttribute("quantities", quantityMap);

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
                            @RequestParam(value = "action", defaultValue = "plus") String action) {
        Cart cart = cartService.findById(1);
        Item item = itemService.findById(itemId);
        int entityQuantity = cartItemService.findQuantityById(itemId);

        if (entityQuantity == 0 && action.equals("minus")) {
            return "redirect:/items/" + itemId;
        }

        if (action.equals("plus")) {
            entityQuantity += 1;
            System.out.println("Cart total price: " + cart.getTotalPrice());
            System.out.println("Item price: " + item.getPrice());
            cart.setTotalPrice(cart.getTotalPrice() + item.getPrice());
        } else {
            entityQuantity -= 1;
            if (entityQuantity == 0) {
                System.out.println("DELETING ITEM FROM CART");
                cartItemService.deleteByItem(item);
                return "redirect:/items/" + itemId;
            }
            cart.setTotalPrice(cart.getTotalPrice() - item.getPrice());
        } // TODO Общая цена корзины при уменьшении товара (если удалялся последний) не меняется

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .item(item)
                .quantity(entityQuantity)
                .build();

        cartItemService.save(cartItem);
        cartService.save(cart);
        return "redirect:/items/" + itemId;
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
