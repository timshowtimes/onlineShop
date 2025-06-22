package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public Integer getTotalQuantity() {
        return cartItemRepository.getTotalQuantity();
    }

    @Transactional
    public void deleteAllByCart(Cart cart) {
        cartItemRepository.deleteAllByCart(cart);
    }

    public List<Item> getAllItems() {
        return cartItemRepository.getAllItems();
    }

    @Transactional
    public void deleteByItem(Item item) {
        cartItemRepository.deleteByItem(item);
    }


    public List<CartItem> getCartItems(Cart cart) {
        return cartItemRepository.findByCart(cart);
    }

    public int findQuantityById(long itemId) {
        return cartItemRepository.findQuantityById(itemId).orElse(0);
    }

    @Transactional
    public void save(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }
}
