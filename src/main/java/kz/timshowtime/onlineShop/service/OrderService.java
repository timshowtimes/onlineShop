package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.Order;
import kz.timshowtime.onlineShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Item> getAllItems() {
        return orderRepository.getAllItems();
    }

    public List<Item> getAllItemsByOrder(long orderId) {
        return orderRepository.getAllItemsByOrder(orderId);
    }

    public Order findById(long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id " + id));
    }

    public long count() {
        return orderRepository.count();
    }
}
