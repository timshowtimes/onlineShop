package kz.timshowtime.onlineShop.model;

import jakarta.persistence.*;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
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

    public Item(Long id, String name, Integer price, String description, byte[] preview) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.preview = preview;
    }

    public String getTextPreview() {
        int nWord = 10;
        if (description == null || description.isEmpty()) return "";
        String[] words = description.split("\\s+");
        if (words.length <= nWord) {
            return description.trim();
        }
        return String.join(" ", Arrays.copyOfRange(words, 0, nWord)) + "...";
    }
}
