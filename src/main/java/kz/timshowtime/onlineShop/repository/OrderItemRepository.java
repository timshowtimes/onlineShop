package kz.timshowtime.onlineShop.repository;


import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderItemRepository extends R2dbcRepository<OrdersItem, Long> {

}
