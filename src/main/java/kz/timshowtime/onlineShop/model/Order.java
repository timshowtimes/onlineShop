package kz.timshowtime.onlineShop.model;

import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    private Long id;

    private String name;

    private Integer totalPrice;

    private LocalDateTime createDt;

    @Transient
    private List<OrdersItem> items = new ArrayList<>();

    public String getTotalPrice() {
        return String.format("%,d тг", totalPrice).replace(",", " ");
    }
}
