package kz.timshowtime.onlineShop.repository;


import kz.timshowtime.onlineShop.model.manyToMany.Embedded.OrderItemId;
import kz.timshowtime.onlineShop.model.manyToMany.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
