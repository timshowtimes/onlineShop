package kz.timshowtime.onlineShop;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.repository.CartItemRepository;
import kz.timshowtime.onlineShop.repository.CartRepository;
import kz.timshowtime.onlineShop.repository.ItemRepository;
import kz.timshowtime.paymentsapp.PaymentsAppApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ImportTestcontainers(TestcontainersConfiguration.class)
public class PaymentsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private static ConfigurableApplicationContext paymentContext;


    @BeforeAll
    void setup() {
        paymentContext = SpringApplication.run(PaymentsAppApplication.class, "--server.port=8082");
    }

    @AfterAll
    void tearDown() {
        paymentContext.close();
    }

    @BeforeEach
    void init() {
        cartItemRepository.deleteAll()
                .then(cartRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .then(
                        itemRepository.save(new Item(null, "Test Item", 19999, null, new byte[2]))
                                .zipWith(cartRepository.save(new Cart(null, 19999)))
                                .flatMap(tuple -> {
                                    Item savedItem = tuple.getT1();
                                    Cart savedCart = tuple.getT2();

                                    CartItem cartItem = new CartItem(
                                            null,
                                            savedCart.getId(),
                                            savedItem.getId(),
                                            1,
                                            LocalDateTime.now()
                                    );
                                    return cartItemRepository.save(cartItem);
                                })
                )
                .block();
    }

    @Test
    void testBuyWithRestPayment() {
        webTestClient.post()
                .uri("/cart/buy")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

}
