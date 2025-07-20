package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.OrderItemDto;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import kz.timshowtime.onlineShop.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final DatabaseClient databaseClient;

    public Mono<Void> saveAll(List<OrdersItem> orderItems) {
        return orderItemRepository.saveAll(orderItems).then();
    }

    public Mono<List<Order>> findAllOrdersWithItems() {
        return findAllWithItems()
                .collect(Collectors.groupingBy(
                        OrderItemDto::getOrderId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .map(grouped -> {
                    List<Order> orders = new ArrayList<>();
                    for (Map.Entry<Long, List<OrderItemDto>> entry : grouped.entrySet()) {
                        List<OrderItemDto> projections = entry.getValue();
                        OrderItemDto first = projections.getFirst();

                        Order order = Order.builder()
                                .id(first.getOrderId())
                                .name(first.getOrderName())
                                .totalPrice(first.getOrderTotalPrice())
                                .createDt(first.getOrderCreateDt())
                                .items(new ArrayList<>())
                                .build();

                        for (OrderItemDto p : projections) {
                            Item item = Item.builder()
                                    .id(p.getItemId())
                                    .name(p.getItemName())
                                    .price(p.getItemPrice())
                                    .description(p.getItemDescription())
                                    .preview(p.getItemPreview())
                                    .quantity(p.getQuantity())
                                    .build();
                            order.getItems().add(item);
                        }

                        orders.add(order);
                    }
                    return orders;
                });
    }


    private Flux<OrderItemDto> findAllWithItems() {
        String sql = """
                    SELECT
                        o.id AS order_id,
                        o.name AS order_name,
                        o.total_price AS order_total_price,
                        o.create_dt AS order_create_dt,
                        i.id AS item_id,
                        i.name AS item_name,
                        i.price AS item_price,
                        i.description AS item_description,
                        i.preview AS item_preview,
                        oi.quantity as quantity
                    FROM orders o
                    JOIN orders_items oi ON o.id = oi.order_id
                    JOIN item i ON oi.item_id = i.id
                    ORDER BY o.create_dt DESC
                """;

        return databaseClient.sql(sql)
                .map((row, meta) -> new OrderItemDto(
                        row.get("order_id", Long.class),
                        row.get("order_name", String.class),
                        row.get("order_total_price", Integer.class),
                        row.get("order_create_dt", LocalDateTime.class),
                        row.get("item_id", Long.class),
                        row.get("item_name", String.class),
                        row.get("item_price", Integer.class),
                        row.get("item_description", String.class),
                        row.get("item_preview", byte[].class),
                        row.get("quantity", Integer.class)
                ))
                .all();
    }


}
