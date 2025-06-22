package kz.timshowtime.onlineShop.model.manyToMany;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.Embedded.OrderItemId;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders_items")
@IdClass(OrderItemId.class)
@Getter
@Setter
public class OrdersItem {

    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private int quantity;

    public String getReadablePriceByQuantity() {
        return String.format("%,d", item.getPrice() * quantity).replace(',', ' ');
    }
}

