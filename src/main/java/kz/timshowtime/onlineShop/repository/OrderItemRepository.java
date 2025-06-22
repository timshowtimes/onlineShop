package kz.timshowtime.onlineShop.repository;


import kz.timshowtime.onlineShop.dto.OrderItemDto;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.Embedded.OrderItemId;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrdersItem, OrderItemId> {


    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items oi JOIN FETCH oi.item")
    List<Order> findAllWithItems();

    @Query("""
    select new kz.timshowtime.onlineShop.dto.OrderItemDto(
        o.id, o.name, o.totalPrice,
        new kz.timshowtime.onlineShop.dto.ItemDto(i.id, i.name, i.price, i.description, i.preview, oi.quantity)
    )
    from Order o
    join OrdersItem oi on o.id = oi.order.id
    join Item i on i.id = oi.item.id
    """)
    List<OrderItemDto> getOrderItemDtoList();
}
