package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;

    @Transactional
    public Mono<Cart> save(Cart cart) {
       return cartRepository.save(cart);
    }

    public Mono<Cart> findById(long id) {
        return cartRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Cart not found with id " + id)));
    }
}
