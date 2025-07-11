package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    private final R2dbcEntityTemplate template;

    public Flux<Order> findAll() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createDt"));
    }

    @Transactional
    public Mono<Order> save(Order order) {
        return orderRepository.save(order);
    }

    public Flux<ItemDto> getAllItemsByOrder(long orderId) {
        String sql = """
        SELECT i.id, i.name, i.price, i.description, i.preview, oi.quantity
        FROM item i
        JOIN orders_item oi ON i.id = oi.item_id
        WHERE oi.order_id = ?
    """;

        return template.getDatabaseClient()
                .sql(sql)
                .bind(0, orderId)
                .map((row, meta) -> new ItemDto(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("price", Integer.class),
                        row.get("description", String.class),
                        row.get("preview", String.class),
                        row.get("quantity", Integer.class)
                ))
                .all();
    }


    public Mono<Order> findById(long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Order not found with id " + id)));
    }

    public Mono<Long> count() {
        return orderRepository.count();
    }
}
