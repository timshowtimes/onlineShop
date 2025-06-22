package kz.timshowtime.onlineShop.dto;

import java.util.Arrays;

public record ItemDto(Long id, String name, Integer price, String description, byte[] preview, Integer quantity) {
    @Override
    public String toString() {
        return "ItemDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}