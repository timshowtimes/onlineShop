package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.model.manyToMany.Embedded.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
//    @Query("SELECT new kz.timshowtime.onlineShop.dto.ItemQuantityDto(ci.item.id, ci.quantity) " +
//            "FROM CartItem ci WHERE ci.cart.id = :cartId")
//    List<ItemQuantityDto> findItemQuantitiesByCartId(@Param("cartId") int cartId);

    @Query("SELECT ci.quantity from CartItem ci WHERE ci.item.id = :itemId")
    Optional<Integer> findQuantityById(@Param("itemId") Long item);

    @Query("SELECT new kz.timshowtime.onlineShop.model.Item(i.id, i.name, i.price, i.description, i.preview, ci.quantity)" +
            " FROM Item i JOIN CartItem ci ON i.id = ci.item.id ORDER BY ci.createDt DESC")
    List<Item> getAllItems();

    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci")
    int getTotalQuantity();

    void deleteByItem(Item item);

    void deleteAllByCart(Cart cart);

    List<CartItem> findByCart(Cart cart);
}
