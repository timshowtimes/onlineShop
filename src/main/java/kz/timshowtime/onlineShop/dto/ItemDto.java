package kz.timshowtime.onlineShop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private byte[] preview;
    private Integer quantity;

    public ItemDto(Long id, String name, Integer price, String description, byte[] preview) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.preview = preview;
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

    public String getTextPreview() {
        return getValidLength(description, 83);
    }

    private String getValidLength(String field, int length) {
        return field.length() <= length ? field : field.substring(0, length) + "...";
    }
}
