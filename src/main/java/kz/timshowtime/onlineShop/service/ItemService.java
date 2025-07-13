package kz.timshowtime.onlineShop.service;

import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    private final R2dbcEntityTemplate template;


    public Flux<ItemDto> findAll(String keyword, Pageable pageable) {
        String pattern = "%" + keyword.toLowerCase() + "%";

        Set<String> allowedSortFields = Set.of("name", "price", "description");

        Optional<String> orderByClause = pageable.getSort().stream()
                .filter(order -> allowedSortFields.contains(order.getProperty()))
                .findFirst()
                .map(order -> "ORDER BY " + order.getProperty() + " " + order.getDirection());

        String sql = """
                    SELECT * FROM item
                    WHERE LOWER(name) LIKE :pattern
                    OR LOWER(description) LIKE :pattern
                    %s
                    LIMIT :limit OFFSET :offset
                """.formatted(orderByClause.orElse(""));

        return template.getDatabaseClient()
                .sql(sql)
                .bind("pattern", pattern)
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, meta) -> new ItemDto(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("price", Integer.class),
                        row.get("description", String.class),
                        row.get("preview", byte[].class)
                ))
                .all();
    }


    public Mono<Item> findById(Long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Item not found with id " + id)));
    }

    @Transactional
    public Mono<Item> save(Item item) {
        return itemRepository.save(item);
    }

    public Mono<Long> count() {
        return itemRepository.count();
    }

    public Mono<byte[]> getImageByPostId(Long itemId) {
        return findById(itemId).map(Item::getPreview);
    }
}
