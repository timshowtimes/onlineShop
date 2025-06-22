package kz.timshowtime.onlineShop.model.manyToMany;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.Embedded.CartItemId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@IdClass(CartItemId.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartItem {

    @Id
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private int quantity;

    private LocalDateTime createDt;
}
