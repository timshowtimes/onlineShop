package kz.timshowtime.onlineShop.controller;

import jakarta.servlet.http.HttpServletResponse;
import kz.timshowtime.onlineShop.enums.SortName;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.ItemPageable;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

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

        model.addAttribute("items", items);
        model.addAttribute("sort", sortValue.getFieldName());
        model.addAttribute("search", name);
        model.addAttribute("paging", paging);

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
        model.addAttribute("item", itemService.getById(id));
        return "item";
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
