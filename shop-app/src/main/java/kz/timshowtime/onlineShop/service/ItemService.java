package kz.timshowtime.onlineShop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.model.Item;
import kz.timshowtime.onlineShop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    private final R2dbcEntityTemplate template;

    private final ObjectMapper mapper;

    private static final String SHOWCASE_ITEMS_KEY = "showcase:items";

    private static final String ITEM_SOLO = "item:solo";

    private final ReactiveRedisTemplate<String, String> pageStringRedisTemplate;

    private final ReactiveRedisTemplate<String, ItemDto> itemRedisTemplate;

    private static final Duration PAGE_TTL = Duration.ofMinutes(2);


    public Flux<ItemDto> findAll(String keyword, Pageable pageable) {
        boolean useCache = keyword.isEmpty() && pageable.getSort().isUnsorted();

        String cacheKey = SHOWCASE_ITEMS_KEY + ":" + "page:%d:%d".formatted(pageable.getPageNumber(), pageable.getPageSize());

        if (!useCache) {
            log.info("⏩ [Витрина] Пропускаем кэш, идём сразу в БД (ключевые слова или сортировка активны)");
            return loadFromDb(keyword, pageable);
        }

        return pageStringRedisTemplate.opsForValue().get(cacheKey)
                .doOnNext(json -> log.info("✅ [Витрина] Данные получены из Redis-кэша по ключу {}", cacheKey))
                .flatMapMany(json -> Mono.fromCallable(() ->
                                        mapper.readValue(json, new TypeReference<List<ItemDto>>() {
                                        }))
                                .onErrorResume(ex -> {
                                    log.warn("Can't deserialize cached page {}, will reload from DB", cacheKey, ex);
                                    return Mono.empty();
                                })
                                .flatMapMany(Flux::fromIterable)
                )
                .switchIfEmpty( // положить в кеш если такого ключа еще нет
                        loadFromDb(keyword, pageable)
                                .collectList()
                                .flatMapMany(list ->
                                        Mono.fromCallable(() -> mapper.writeValueAsString(list))
                                                .flatMapMany(json -> pageStringRedisTemplate.opsForValue()
                                                        .set(cacheKey, json, PAGE_TTL)
                                                        .doOnSuccess(saved ->
                                                                log.info("📦 [Витрина] Кэша нет — сохранили страницу [{}] в Redis на {} сек", cacheKey, PAGE_TTL.getSeconds())
                                                        )
                                                        .flatMapMany(saved -> Flux.fromIterable(list))
                                                )
                                )
                );
    }

    private Flux<ItemDto> loadFromDb(String keyword, Pageable pageable) {
        String pattern = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";

        Set<String> allowedSortFields = Set.of("name", "price", "description");

        Optional<String> orderByClause = pageable.getSort().stream()
                .filter(order -> allowedSortFields.contains(order.getProperty()))
                .findFirst()
                .map(order -> "ORDER BY " + order.getProperty() + " " + order.getDirection());

        String sql = """
                    SELECT id, name, price, description, preview
                    FROM item
                    WHERE LOWER(name) LIKE :pattern
                       OR LOWER(description) LIKE :pattern
                    %s
                    LIMIT :limit OFFSET :offset
                """.formatted(orderByClause.orElse(""));

        return template.getDatabaseClient().sql(sql)
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


    public Mono<ItemDto> findById(Long id) {
        Mono<ItemDto> itemFromCache = itemRedisTemplate.opsForHash()
                .get(ITEM_SOLO, id)
                .cast(ItemDto.class);

        return itemFromCache.hasElement()
                .flatMap(cached -> {
                    if (cached) {
                        log.info("✅ [Карточка товара] Данные получены из Redis-кэша по ключу {}", ITEM_SOLO);
                        return itemFromCache;
                    } else {
                        log.info("📦 [Карточка товара] Кэш пуст — загружаем из БД и кладем в Redis");
                        return itemRepository.findById(id)
                                .map(item -> mapper.convertValue(item, ItemDto.class))
                                .switchIfEmpty(Mono.error(new NoSuchElementException("Item not found with id " + id)))
                                .flatMap(item -> itemRedisTemplate.opsForHash()
                                        .put(ITEM_SOLO, id, item)
                                        .then(itemRedisTemplate.expire(ITEM_SOLO, Duration.ofMinutes(2)))
                                        .thenReturn(item)
                                );

                    }
                });
    }


    @Transactional
    public Mono<Item> save(Item item) {
        return itemRepository.save(item);
    }

    public Mono<Long> count() {
        return itemRepository.count();
    }

    public Mono<byte[]> getImageByPostId(Long itemId) {
        return findById(itemId).map(ItemDto::getPreview);
    }
}
