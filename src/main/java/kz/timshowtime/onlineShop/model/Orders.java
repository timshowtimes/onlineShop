package kz.timshowtime.onlineShop.model;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer totalPrice;

    private LocalDateTime createDt;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersItem> items = new ArrayList<>();
}
