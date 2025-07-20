package kz.timshowtime.onlineShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderItemDto {
    private Long orderId;
    private String orderName;
    private Integer orderTotalPrice;
    private LocalDateTime orderCreateDt;
    private Long itemId;
    private String itemName;
    private Integer itemPrice;
    private String itemDescription;
    private byte[] itemPreview;
    private Integer quantity;
}
