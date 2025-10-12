package kz.timshowtime.onlineShop;

import kz.timshowtime.onlineShop.config.SecurityConfig;
import kz.timshowtime.onlineShop.dto.ItemDto;


import kz.timshowtime.onlineShop.controller.CartControllerReactive;
import kz.timshowtime.onlineShop.controller.ItemControllerReactive;
import kz.timshowtime.onlineShop.enums.Role;
import kz.timshowtime.onlineShop.factory.WalletsApiFactory;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.ClientUser;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
import kz.timshowtime.onlineShop.paymentsclient.model.ChargeRequest;
import kz.timshowtime.onlineShop.paymentsclient.model.ChargeResult;
import kz.timshowtime.onlineShop.security.ClientUserDetailsService;
import kz.timshowtime.onlineShop.security.UserDetailsImpl;
import kz.timshowtime.onlineShop.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;

@WebFluxTest(controllers = {ItemControllerReactive.class, CartControllerReactive.class})
@Import(SecurityConfig.class)
@AutoConfigureWebTestClient
class MvcTests {

    @Autowired
    private WebTestClient webTestClient;

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

    @MockitoBean(reset = MockReset.BEFORE)
    private WalletsApiFactory walletsApiFactory;

    @MockitoBean(reset = MockReset.BEFORE)
    private ClientUserDetailsService clientUserDetailsService;

    @Mock
    private WalletsApi walletsApi;

    private UserDetailsImpl testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserDetailsImpl(
                ClientUser.builder()
                        .id(1L)
                        .username("test_user")
                        .password("test_password")
                        .role(Role.ROLE_USER)
                        .build()
        );
    }


    @BeforeAll
    static void enableReactorDebug() {
        Hooks.onOperatorDebug();
    }

    @Test
    public void getAllItem() {
        when(itemService.findAll(any(String.class), any(Pageable.class)))
                .thenReturn(Flux.just(
                        createItemDto(1L, "Smartphone TestPhone X1", 19999, "Test desc 1", 0),
                        createItemDto(2L, "Smartphone TestPhone X2", 18888, "Test desc 2", 0),
                        createItemDto(3L, "Smartphone TestPhone X3", 17777, "Test desc 3", 0)
                ));

        when(itemService.count()).thenReturn(Mono.just(3L));

        when(cartItemService.getAllItemsByUserId(true, 0L, testUser.getId())).thenReturn(Flux.just(
                createItemDto(1L, "Smartphone TestPhone X1", 19999, "Test desc 1", 2),
                createItemDto(2L, "Smartphone TestPhone X2", 18888, "Test desc 2", 1),
                createItemDto(3L, "Smartphone TestPhone X3", 17777, "Test desc 3", 3)
        ));

        when(cartItemService.getTotalQuantityByUserId(1L)).thenReturn(Mono.just(3));

        webTestClient.mutateWith(
                        mockAuthentication(
                                new UsernamePasswordAuthenticationToken(testUser, testUser.getPassword(), testUser.getAuthorities())
                        ))
                .get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("Smartphone TestPhone X1"));
                    assertTrue(body.contains("Test desc 2"));
                });

        verify(itemService, times(1)).findAll(any(String.class), any(Pageable.class));
        verify(itemService, times(1)).count();
        verify(cartItemService, times(1)).getAllItemsByUserId(true, 0L, 1L);
        verify(cartItemService, times(1)).getTotalQuantityByUserId(1L);
    }


    @Test
    public void putOnCart() {
        ItemDto item = createItem(1L, "Smartphone TestPhone X1", 19999, "Test desc 1");
        Cart cart = createCart(0);

        when(itemService.findById(1L)).thenReturn(Mono.just(item));
        when(cartService.createCartForUser(testUser.getId())).thenReturn(Mono.just(cart));
        when(cartService.findByUserId(testUser.getId())).thenReturn(Mono.just(cart));
        when(cartItemService.findQuantityByItemId(1L, testUser.getId())).thenReturn(Mono.just(0));
        when(cartItemService.saveOrUpdate(any())).thenReturn(Mono.empty());
        when(cartService.save(cart)).thenReturn(Mono.empty());

        webTestClient.mutateWith(
                        mockAuthentication(new UsernamePasswordAuthenticationToken(testUser, testUser.getPassword(), testUser.getAuthorities())
                        ))
                .post()
                .uri("/cart/{id}", 1L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("action", "plus")
                        .with("source", "items"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/items?cache=false");

        verify(itemService, times(1)).findById(1L);
        verify(cartService, times(1)).save(any());
        verify(cartService, times(1)).createCartForUser(testUser.getId());
        verify(cartService, times(1)).findByUserId(1L);
        verify(cartItemService, times(1)).findQuantityByItemId(1L, testUser.getId());
        verify(cartItemService, times(1)).saveOrUpdate(any());

    }


    @Test
    public void buy() {
        Cart cart = createCart(19999);
        CartItem cartItem = CartItem.builder().cartId(1L).itemId(2L).quantity(1).build();

        Order order = Order.builder().id(42L).name("Заказ №...").totalPrice(19999).build();

        when(walletsApiFactory.create()).thenReturn(walletsApi);

        ChargeResult mockChargeResult = new ChargeResult()
                .paymentId(UUID.randomUUID())
                .debited(1000.0)
                .balanceAfter(2000.0)
                .status(ChargeResult.StatusEnum.APPROVED)
                .message("The testing payment was successful");

        when(clientUserDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(
                Mono.just(ClientUser.builder()
                        .id(testUser.getId())
                        .username(testUser.getUsername())
                        .password(testUser.getPassword())
                        .role(Role.ROLE_USER)
                        .build())
        );
        when(walletsApi.charge(anyLong(), any(ChargeRequest.class))).thenReturn(Mono.just(mockChargeResult));
        when(orderService.getNextOrderId()).thenReturn(Mono.just(42L));
        when(cartService.findByUserId(testUser.getId())).thenReturn(Mono.just(cart));
        when(cartService.save(cart)).thenReturn(Mono.just(cart));
        when(cartItemService.getCartItems(cart.getId())).thenReturn(Flux.just(cartItem));
        when(cartItemService.deleteAllByCart(cart)).thenReturn(Mono.empty());
        when(orderService.countByUserId(testUser.getId())).thenReturn(Mono.just(5L));
        when(orderService.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemService.saveAll(any())).thenReturn(Mono.empty());


        webTestClient.mutateWith(
                        mockAuthentication(new UsernamePasswordAuthenticationToken(testUser, testUser.getPassword(), testUser.getAuthorities())
                        ))
                .post()
                .uri("/cart/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/orders/42?new=true");

        verify(orderService, times(1)).save(any(Order.class));
        verify(cartItemService, times(1)).deleteAllByCart(cart);
        verify(cartService, times(1)).save(any());
    }

    public static Cart createCart(int price) {
        return Cart.builder()
                .totalPrice(price)
                .userId(1L)
                .build();
    }

    public static ItemDto createItem(Long id, String name, Integer price, String description) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .price(price)
                .description(description)
                .preview(new byte[]{1, 2, 3})
                .build();
    }

    public static ItemDto createItemDto(Long id, String name, Integer price, String description, Integer quantity) {
        return new ItemDto(
                id,
                name,
                price,
                description,
                new byte[]{1, 2, 3},
                quantity
        );
    }


}
