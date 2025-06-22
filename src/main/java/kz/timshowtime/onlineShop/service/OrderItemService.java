package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.OrderItemDto;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.model.manyToMany.OrdersItem;
import kz.timshowtime.onlineShop.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public List<OrdersItem> findAll() {
        return orderItemRepository.findAll();
    }
}
