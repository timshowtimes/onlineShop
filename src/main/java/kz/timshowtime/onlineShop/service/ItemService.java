package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    public Item getById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id " + id));
    }

    public void save(Item item) {
        itemRepository.save(item);
    }
}
