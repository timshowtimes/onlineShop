package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.Cart;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    private final ReactiveRedisTemplate<String, ItemDto> cartItemRedisTemplate;

    public static final String CART_ITEMS_KEY = "cart:items";

    public Mono<Integer> getTotalQuantity() {
        return cartItemRepository.getTotalQuantity();
    }

    @Transactional
    public Mono<Void> deleteAllByCart(Cart cart) {
        return cartItemRepository.deleteAllByCartId(cart.getId());
    }

    public Mono<Void> evictCartItemById(Long id) {
        if (id == -1L) {
            return cartItemRedisTemplate.delete(CART_ITEMS_KEY).then();
        }
        return cartItemRedisTemplate.opsForHash()
                .remove(CART_ITEMS_KEY, id.toString())
                .then();
    }


    public Flux<ItemDto> getAllItems(Boolean cache, Long evictById) {
        Flux<ItemDto> cached = cartItemRedisTemplate.opsForHash()
                .values(CART_ITEMS_KEY)
                .cast(ItemDto.class);

        Mono<Void> eviction = Mono.empty();
        if (evictById != 0L) {
            log.info("🗑️ [Корзина] Удаляем товар {} из кэша {}", evictById, CART_ITEMS_KEY);
            eviction = evictCartItemById(evictById);
        }

        return eviction.thenMany(cached.hasElements()
                .flatMapMany(hasCache -> {
                    if (hasCache && cache) {
                        log.info("✅ [Корзина] Данные получены из Redis-кэша по ключу {}", CART_ITEMS_KEY);
                        return cached;
                    } else {
                        log.info("📦 [Корзина] Кэш пуст — загружаем из БД и кладем в Redis");
                        return cartItemRepository.getAllItems()
                                .collectList()
                                .flatMapMany(items -> Flux.fromIterable(items)
                                        .flatMap(item ->
                                                cartItemRedisTemplate.opsForHash()
                                                        .put(CART_ITEMS_KEY, item.getId(), item)
                                                        .then(cartItemRedisTemplate.expire(CART_ITEMS_KEY, Duration.ofMinutes(2)))
                                                        .thenReturn(item)
                                        )
                                );
                    }
                })
        );
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
