//package kz.timshowtime.onlineShop;
//
//import kz.timshowtime.onlineShop.enums.Role;
//import kz.timshowtime.onlineShop.factory.WalletsApiFactory;
//import kz.timshowtime.onlineShop.model.Cart;
//import kz.timshowtime.onlineShop.model.ClientUser;
//import kz.timshowtime.onlineShop.model.Item;
//import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
//import kz.timshowtime.onlineShop.paymentsclient.ApiClient;
//import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
//import kz.timshowtime.onlineShop.repository.CartItemRepository;
//import kz.timshowtime.onlineShop.repository.CartRepository;
//import kz.timshowtime.onlineShop.repository.ItemRepository;
//import kz.timshowtime.onlineShop.security.UserDetailsImpl;
//import kz.timshowtime.paymentsapp.PaymentsAppApplication;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.context.ImportTestcontainers;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ImportTestcontainers(TestcontainersConfiguration.class)
//@AutoConfigureWebTestClient
//public class PaymentsIntegrationTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private CartRepository cartRepository;
//
//    private static WalletsApi walletsApi;
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    @Autowired
//    private WalletsApiFactory walletsApiFactory;
//
//    @Autowired
//    private CartItemRepository cartItemRepository;
//
//    private static ConfigurableApplicationContext paymentContext;
//
//
//    @BeforeAll
//    void setup() {
//        paymentContext = SpringApplication.run(PaymentsAppApplication.class,
//                "--server.port=8082",
//                "--spring.profiles.active=test");
//        walletsApi = walletsApiFactory.create();
//    }
//
//    @AfterAll
//    void tearDown() {
//        paymentContext.close();
//    }
//
//    @BeforeEach
//    void init() {
//        cartItemRepository.deleteAll()
//                .then(cartRepository.deleteAll())
//                .then(itemRepository.deleteAll())
//                .then(
//                        itemRepository.save(new Item(null, "Test Item", 1_000_000, null, new byte[2]))
//                                .zipWith(cartRepository.save(new Cart(null, 1_000_000, 1L)))
//                                .flatMap(tuple -> {
//                                    Item savedItem = tuple.getT1();
//                                    Cart savedCart = tuple.getT2();
//
//                                    CartItem cartItem = new CartItem(
//                                            null,
//                                            savedCart.getId(),
//                                            savedItem.getId(),
//                                            1,
//                                            LocalDateTime.now()
//                                    );
//                                    return cartItemRepository.save(cartItem);
//                                })
//                )
//                .block();
//    }
//
//    @Test
//    void testBuyWithRestPaymentAndCheckedBalance() {
//        var user = new UserDetailsImpl(
//                ClientUser
//                        .builder()
//                        .id(1L)
//                        .username("test_user")
//                        .password("test_password")
//                        .role(Role.ROLE_USER)
//                        .build()
//        );
//
//        Double initBalance = walletsApi.getBalance(1L).block().getBalance();
//
//        webTestClient.mutateWith(
//                mockAuthentication(
//                        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities())
//                ))
//                .post()
//                .uri("/cart/buy")
//                .exchange()
//                .expectStatus().is3xxRedirection();
//
//        System.out.println("Init balance: " + initBalance);
//
//        Double finalBalance = walletsApi.getBalance(1L).block().getBalance();
//
//        System.out.println("Final balance: " + finalBalance);
//        assertThat(finalBalance).isEqualTo(initBalance - 1_000_000);
//    }
//
//}
