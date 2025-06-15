package kz.timshowtime.onlineShop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@IdClass(OrderItemId.class)
@Data
public class OrderItem {

    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private int quantity;
}

