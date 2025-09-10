package kz.timshowtime.onlineShop;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.repository.CartItemRepository;
import kz.timshowtime.onlineShop.repository.CartRepository;
import kz.timshowtime.onlineShop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataR2dbcTest
@ImportTestcontainers(TestcontainersConfiguration.class)
@Testcontainers
public class DaoTests {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;


    @Test
    void testItemSave() {
        Item item = createItemWithoutId();

        StepVerifier.create(itemRepository.save(item))
                .expectNextMatches(savedItem ->
                        savedItem.getId() != null &&
                                savedItem.getName().equals(item.getName())
                )
                .verifyComplete();

    }

    @Test
    void testItemFindById() {
        Item item = createItemWithoutId();

        StepVerifier.create(itemRepository.save(item).
                        flatMap(savedItem -> itemRepository.findById(savedItem.getId())))
                .assertNext(foundItem -> {
                    assertThat(foundItem.getId()).isEqualTo(item.getId());
                    assertThat(foundItem.getName()).isEqualTo(item.getName());
                })
                .verifyComplete();

    }


    @Test
    void testPutOnCartItem() {
        Item item1 = createItemWithoutId();
        Item item2 = createItemWithoutId();
        Cart cart = MvcTests.createCart(19999);

        Mono<Integer> totalQuantityMono = Mono.zip(itemRepository.save(item1), itemRepository.save(item2), cartRepository.save(cart))
                .flatMap(tuple -> {
                    Item saved1 = tuple.getT1();
                    Item saved2 = tuple.getT2();
                    Cart savedCart = tuple.getT3();

                    return Flux.concat(
                                    cartItemRepository.save(
                                            CartItem.builder()
                                                    .cartId(savedCart.getId())
                                                    .itemId(saved1.getId())
                                                    .quantity(5)
                                                    .build()
                                    ),
                                    cartItemRepository.save(
                                            CartItem.builder()
                                                    .cartId(savedCart.getId())
                                                    .itemId(saved2.getId())
                                                    .quantity(5)
                                                    .build()
                                    )
                            )
                            .then(cartItemRepository.getTotalQuantity());

                });

                StepVerifier.create(totalQuantityMono)
                        .expectNext(10)
                        .verifyComplete();
    }


    private Item createItemWithoutId() {
        Item item = new Item();
        item.setName("Smartphone TestPhone X1");
        item.setPrice(19999);
        item.setDescription("Test desc 1");
        return item;
    }
}
