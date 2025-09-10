package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {

    @Query("SELECT COALESCE(SUM(quantity), 0) AS quantity FROM cart_items WHERE item_id = :itemId")
    Mono<Integer> findQuantityByItemId(@Param("itemId") Long itemId);

    @Query("""
       INSERT INTO cart_items (cart_id, item_id, quantity, create_dt)
       VALUES (:#{#ci.cartId}, :#{#ci.itemId}, :#{#ci.quantity}, :#{#ci.createDt})
       ON CONFLICT (cart_id, item_id) DO UPDATE
         SET quantity  = EXCLUDED.quantity,
             create_dt = EXCLUDED.create_dt
       RETURNING *
       """)
    Mono<CartItem> saveOrUpdate(@Param("ci") CartItem ci);

    @Query("""
        SELECT i.id, i.name, i.price, i.description, i.preview, ci.quantity
        FROM item i
        JOIN cart_items ci ON i.id = ci.item_id
        ORDER BY ci.create_dt DESC
    """)
    Flux<ItemDto> getAllItems();


    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    Mono<Integer> getTotalQuantity();


    Mono<Void> deleteByItemId(Long itemId);

    Mono<Void> deleteAllByCartId(Long cartId);

    Flux<CartItem> findByCartId(Long cartId);
}
