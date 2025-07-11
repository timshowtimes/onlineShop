package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
    @Query("SELECT new kz.timshowtime.onlineShop.model.Item(i.id, i.name, i.price, i.description, i.preview, oi.quantity) " +
            "FROM Item i JOIN OrdersItem oi ON i.id = oi.item.id where oi.order.id = :orderId")
    List<Item> getAllItemsByOrder(@Param("orderId") Long orderId);

//    @Query("SELECT new kz.timshowtime.onlineShop.model.Item(i.id, i.name, i.price, i.description, i.preview, oi.quantity) " +
//            "FROM Item i JOIN OrdersItem oi ON i.id = oi.item.id")
//    List<Item> getAllItems();
}
