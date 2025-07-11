package kz.timshowtime.onlineShop.model;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    private String description;

    private byte[] preview;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersItem> orders = new ArrayList<>();

    @Transient
    private int quantity;

    public Item(Long id, String name, Integer price, String description, byte[] preview) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.preview = preview;
    }

    public Item(Long id, String name, Integer price, String description, byte[] preview, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.preview = preview;
        this.quantity = quantity;
    }

    public String getReadablePrice() {
        return String.format("%,d тг.", price).replace(',', ' ');
    }

    public String getReadablePriceByQuantity() {
        return String.format("%,d тг.", price * quantity).replace(',', ' ');

    }

    public String getValidName() {
        return getValidLength(name, 40);
    }

    private String getValidLength(String field, int length) {
        return field.length() <= length ? field : field.substring(0, length) + "...";
    }

    public String getTextPreview() {
        return getValidLength(description, 83);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", orders=" + orders +
                ", quantity=" + quantity +
                '}';
    }
}
