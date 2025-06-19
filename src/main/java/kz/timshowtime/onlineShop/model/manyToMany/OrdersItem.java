package kz.timshowtime.onlineShop.model.manyToMany;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Orders;
import kz.timshowtime.onlineShop.model.manyToMany.Embedded.OrderItemId;
import lombok.Data;

@Entity
@Table(name = "orders_items")
@IdClass(OrderItemId.class)
@Data
public class OrdersItem {

    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders orders;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private int quantity;
}

