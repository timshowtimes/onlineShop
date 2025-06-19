package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.ItemQuantityDto;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.model.manyToMany.CartItem;
import kz.timshowtime.onlineShop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public List<Item> getAllItems() {
        return cartItemRepository.getAllItems();
    }

    @Transactional
    public void deleteByItem(Item item) {
        cartItemRepository.deleteByItem(item);
    }

    public List<ItemQuantityDto> getItemQuantities(int cartId) {
        return cartItemRepository.findItemQuantitiesByCartId(cartId);
    }

    public int findQuantityById(long itemId) {
        return cartItemRepository.findQuantityById(itemId).orElse(0);
    }

    @Transactional
    public void save(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }
}
