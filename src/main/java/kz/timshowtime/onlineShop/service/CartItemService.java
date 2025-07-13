package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    private final R2dbcEntityTemplate template;

    public Mono<Integer> getTotalQuantity() {
        return cartItemRepository.getTotalQuantity();
    }

    @Transactional
    public Mono<Void> deleteAllByCart(Cart cart) {
        return cartItemRepository.deleteAllByCartId(cart.getId());
    }

    public Flux<ItemDto> getAllItems() {
        return cartItemRepository.getAllItems();
    }

    @Transactional
    public Mono<Void> deleteByItemId(Long itemId) {
       return cartItemRepository.deleteByItemId(itemId);
    }


    public Flux<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    public Mono<Integer> findQuantityByItemId(long itemId) {
        return cartItemRepository.findQuantityByItemId(itemId);
    }

    @Transactional
    public Mono<CartItem> saveOrUpdate(CartItem cartItem) {
       return cartItemRepository.saveOrUpdate(cartItem);
    }
}
