package kz.timshowtime.onlineShop;

import kz.timshowtime.onlineShop.controller.CartController;
import kz.timshowtime.onlineShop.controller.ItemController;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@ImportTestcontainers(TestcontainersConfiguration.class)
//@Testcontainers
@WebMvcTest(controllers = {ItemController.class, CartController.class})
class MvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(reset = MockReset.BEFORE)
    private ItemService itemService;

    @MockitoBean(reset = MockReset.BEFORE)
    private CartService cartService;

    @MockitoBean(reset = MockReset.BEFORE)
    private CartItemService cartItemService;

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderService orderService;

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderItemService orderItemService;

    @Test
    public void getAllItem() throws Exception {
        when(itemService.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(List.of(
                        createItem(1L, "Smartphone TestPhone X1", 19999, "Test desc 1"),
                        createItem(2L, "Smartphone TestPhone X2", 18888, "Test desc 2"),
                        createItem(3L, "Smartphone TestPhone X3", 17777, "Test desc 3")
                ));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("items"))
                .andExpect(view().name("main"))
                .andExpect(content().string(containsString("Test desc 1")))
                .andExpect(content().string(containsString("Test desc 2")))
                .andExpect(content().string(containsString("Test desc 3")));

        verify(itemService, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(cartItemService, times(1)).getAllItems();

    }


    @Test
    public void putOnCart() throws Exception {
        Item item = createItem(1L, "Smartphone TestPhone X1", 19999, "Test desc 1");
        Cart cart = createCart(0);

        when(itemService.findById(1L)).thenReturn(item);
        when(cartService.findById(1)).thenReturn(cart);
        when(cartItemService.findQuantityById(1L)).thenReturn(0);

        mockMvc.perform(post("/items/1")
                .param("action", "plus")
                .param("source", "items"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(itemService, times(1)).findById(1L);
        verify(cartService, times(1)).save(any());
        verify(cartItemService, times(1)).findQuantityById(1L);
        verify(cartItemService, times(1)).save(any());

    }

    @Test
    public void buy() throws Exception {
        Item item = createItem(1L, "Smartphone TestPhone X1", 19999, "Test desc 1");
        Cart cart = createCart(19999);
        CartItem cartItem = CartItem.builder().cart(cart).item(item).quantity(1).build();

        Order order = Order.builder().id(42L).name("Заказ №...").totalPrice(19999).build();

        when(cartService.findById(1)).thenReturn(cart);
        when(cartItemService.getCartItems(cart)).thenReturn(List.of(cartItem));
        when(orderService.count()).thenReturn(5L);
        when(orderService.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/cart/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/42?new=true"));

        verify(orderService, times(1)).save(any(Order.class));
        verify(cartItemService, times(1)).deleteAllByCart(cart);
        verify(cartService, times(1)).save(any());
    }

    public static Cart createCart(int price) {
        return Cart.builder()
                .id(1)
                .totalPrice(price)
                .build();
    }


    public static Item createItem(Long id, String name, Integer price, String description) {
        return Item.builder()
                .id(id)
                .name(name)
                .price(price)
                .description(description)
                .preview(new byte[]{1, 2, 3})
                .build();
    }


}
