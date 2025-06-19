package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public List<Item> findAll(Specification<Item> spec, Pageable page) {
        return itemRepository.findAll(spec, page).getContent();
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id " + id));
    }

    @Transactional
    public void save(Item item) {
        itemRepository.save(item);
    }

    public long count() {
       return itemRepository.count();
    }

    public byte[] getImageByPostId(Long itemId) {
        return findById(itemId).getPreview();
    }
}
