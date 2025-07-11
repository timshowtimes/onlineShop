package kz.timshowtime.onlineShop.dto;

public record ItemDto(
        Long id,
        String name,
        Integer price,
        String description,
        String preview,
        Integer quantity
) {}
