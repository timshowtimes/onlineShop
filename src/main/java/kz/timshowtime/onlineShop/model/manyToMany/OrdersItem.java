package kz.timshowtime.onlineShop.model.manyToMany;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "orders_items")
@Getter
@Setter
@Builder
public class OrdersItem {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Column("quantity")
    private int quantity;

//    public String getReadablePriceByQuantity() {
//        return String.format("%,d", item.getPrice() * quantity).replace(',', ' ');
//    }
}

