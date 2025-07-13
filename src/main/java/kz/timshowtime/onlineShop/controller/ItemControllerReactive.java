package kz.timshowtime.onlineShop.controller;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.enums.SortName;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.ItemPageable;
import kz.timshowtime.onlineShop.service.CartItemService;
import kz.timshowtime.onlineShop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemControllerReactive {
    private final ItemService itemService;
    private final CartItemService cartItemService;

    @Value("${server.port}")
    private String port;


    @GetMapping
    public Mono<String> all(@RequestParam(value = "search", required = false) String name,
                            @RequestParam(value = "sort", defaultValue = "NO") SortName sortValue,
                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                            Model model) {

        String keyword = (name == null || name.isBlank()) ? "" : name;

        Sort sort = sortValue.getFieldName().equals("no")
                ? Sort.unsorted()
                : Sort.by(sortValue.getFieldName().toLowerCase());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Mono<Long> totalItemCountMono = itemService.count();
        Mono<List<ItemDto>> itemsMono = itemService.findAll(keyword, pageable).collectList();
        Mono<List<ItemDto>> cartItemsMono = cartItemService.getAllItems().collectList();
        Mono<Integer> totalQuantityMono = cartItemService.getTotalQuantity();

        return Mono.zip(itemsMono, totalItemCountMono, cartItemsMono, totalQuantityMono)
                .map(tuple -> {
                    List<ItemDto> items = tuple.getT1();
                    Long totalItemCount = tuple.getT2();
                    List<ItemDto> cartItems = tuple.getT3();
                    Integer totalQuantity = tuple.getT4();

                    ItemPageable paging = ItemPageable.getPageable(pageNumber, pageSize, totalItemCount);

                    // карта количества товаров в корзине
                    Map<Long, Integer> quantityMap = cartItems.stream()
                            .collect(Collectors.toMap(ItemDto::getId, ItemDto::getQuantity));

                    model.addAttribute("items", items);
                    model.addAttribute("sort", sortValue.name());
                    model.addAttribute("search", name);
                    model.addAttribute("paging", paging);
                    model.addAttribute("quantities", quantityMap);
                    model.addAttribute("totalQuantity", totalQuantity);
                    model.addAttribute("port", port);

                    return "main";
                });
    }


    @GetMapping("/images/{id}")
    public Mono<Void> getPostImage(@PathVariable("id") Long postId, ServerHttpResponse response) {
        return itemService.getImageByPostId(postId)
                .flatMap(image -> {
                    response.getHeaders().setContentType(MediaType.IMAGE_JPEG);
                    response.getHeaders().setContentLength(image.length);
                    DataBuffer buffer = response.bufferFactory().wrap(image);
                    return response.writeWith(Mono.just(buffer));
                });
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getOne(@PathVariable("id") Long id) {
        Mono<Integer> quantityMono = cartItemService.findQuantityByItemId(id);
        Mono<Item> itemMono = itemService.findById(id);

        System.out.println("==== CALLED NEW METHOD ====");

        return Mono.zip(itemMono, quantityMono)
                .map(tuple -> {
                    return Rendering.view("item")
                            .modelAttribute("item", tuple.getT1())
                            .modelAttribute("quantity", tuple.getT2())
                            .build();
                });
    }

    @GetMapping("/add-page")
    public String addPage() {
        return "item-add";
    }

    @PostMapping("/upload")
    public Mono<String> addItem(@RequestPart("image") FilePart image,
                                @RequestPart("name") String name,
                                @RequestPart("price") Integer price,
                                @RequestPart("text") String text) {

        return DataBufferUtils.join(image.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer); // ручное освобождение памяти (Netty)

                    Item item = Item.builder()
                            .preview(bytes)
                            .name(name)
                            .price(price)
                            .description(text)
                            .build();

                    return itemService.save(item).thenReturn("redirect:/items");
                });
    }
}
