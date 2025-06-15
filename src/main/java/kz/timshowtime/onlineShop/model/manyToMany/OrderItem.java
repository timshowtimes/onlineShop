package kz.timshowtime.onlineShop.model.manyToMany;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.Embedded.OrderItemId;
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

