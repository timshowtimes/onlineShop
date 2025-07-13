//package kz.timshowtime.onlineShop;
//
//import kz.timshowtime.onlineShop.model.Cart;
//import kz.timshowtime.onlineShop.model.Item;
//import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
//import kz.timshowtime.onlineShop.repository.CartItemRepository;
//import kz.timshowtime.onlineShop.repository.CartRepository;
//import kz.timshowtime.onlineShop.repository.ItemRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.testcontainers.context.ImportTestcontainers;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@ImportTestcontainers(TestcontainersConfiguration.class)
//@Testcontainers
//public class DaoTests {
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    @Autowired
//    private CartItemRepository cartItemRepository;
//
//    @Autowired
//    private CartRepository cartRepository;
//
//
//    @Test
//    void testItemSave() {
//        Item item = createItemWithoutId();
//        Item savedItem = itemRepository.save(item);
//
//        assertThat(savedItem.getId()).isNotNull();
//        assertThat(savedItem.getName()).isEqualTo(item.getName());
//    }
//
//    @Test
//    void testItemFindById() {
//        Item item = createItemWithoutId();
//        itemRepository.save(item);
//
//        Optional<Item> foundItem = itemRepository.findById(item.getId());
//
//        assertThat(foundItem).isPresent();
//        assertThat(foundItem.get().getName()).isEqualTo(item.getName());
//        assertThat(foundItem.get().getId()).isEqualTo(item.getId());
//
//    }
//
//    @Test
//    void testPutOnCartItem() {
//        Item item1 = createItemWithoutId();
//        Item item2 = createItemWithoutId();
//        Item savedItem1 = itemRepository.save(item1);
//        Item savedItem2 = itemRepository.save(item2);
//
//        Cart cart = MvcTests.createCart(19999);
//        cartRepository.save(cart);
//
//        CartItem cartItem1 = CartItem.builder().cart(cart).item(savedItem1).quantity(5).build();
//        CartItem cartItem2 = CartItem.builder().cart(cart).item(savedItem2).quantity(5).build();
//        cartItemRepository.save(cartItem1);
//        cartItemRepository.save(cartItem2);
//
//        int totalQuantity = cartItemRepository.getTotalQuantity();
//
//        assertThat(totalQuantity).isEqualTo(10);
//    }
//
//    private Item createItemWithoutId() {
//        Item item = new Item();
//        item.setName("Smartphone TestPhone X1");
//        item.setPrice(19999);
//        item.setDescription("Test desc 1");
//        return item;
//    }
//}
