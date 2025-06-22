package kz.timshowtime.onlineShop.model;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer totalPrice;

    private LocalDateTime createDt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersItem> items = new ArrayList<>();

    public String getTotalPrice() {
        return String.format("%,d тг", totalPrice).replace(",", " ");
    }
}
