package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;

    @Transactional
    public void save(Cart cart) {
        cartRepository.save(cart);
    }

    @Transactional
    public void deleteAllById(int id) {
        cartRepository.deleteById(id);
    }

    public Cart findById(int id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cart not found with id " + id));
    }
}
