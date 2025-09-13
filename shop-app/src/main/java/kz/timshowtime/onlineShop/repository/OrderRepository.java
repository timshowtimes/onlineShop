package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.model.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    @Query("SELECT COALESCE(MAX(o.id), 0) + 1 FROM orders o")
    Mono<Long> getNextOrderId();
}
