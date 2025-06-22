package kz.timshowtime.onlineShop.dto;


public record OrderItemDto(Long orderId, String orderName, Integer totalPrice, ItemDto item) {}
