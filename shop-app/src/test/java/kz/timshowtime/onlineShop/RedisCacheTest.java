package kz.timshowtime.onlineShop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.timshowtime.onlineShop.dto.ItemDto;
import kz.timshowtime.onlineShop.service.CartItemService;
import kz.timshowtime.onlineShop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

@SpringBootTest
public class RedisCacheTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ReactiveRedisTemplate<String, String> pageStringRedisTemplate;

    @Autowired
    private ReactiveRedisTemplate<String, ItemDto> itemRedisTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void showcaseShouldReturnFromCache() throws JsonProcessingException {
        Pageable pageable = PageRequest.of(0, 2);
        String cacheKey = "showcase:items:page:0:2";

        List<ItemDto> cached = List.of(new ItemDto(42L, "From Cache", 999, "cached desc", null));
        String json = mapper.writeValueAsString(cached);

        pageStringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(10)).block();

        StepVerifier.create(itemService.findAll("", pageable))
                .expectNextMatches(dto -> dto.getName().equals("From Cache"))
                .verifyComplete();

    }

    @Test
    void cartShouldReturnFromCache() {
        String cacheKey = "cart:items";

        ItemDto cached = new ItemDto(43L, "From Cache 2", 988, "cached desc", null);

        itemRedisTemplate.opsForHash().put(cacheKey, cached.getId(), cached)
                .then(itemRedisTemplate.expire(cacheKey, Duration.ofSeconds(10)))
                .block();

        StepVerifier.create(cartItemService.getAllItems(Boolean.TRUE, 0L))
                .expectNextMatches(dto -> dto.getName().equals("From Cache 2"))
                .verifyComplete();
    }

    @Test
    void itemsShouldReturnFromCache() {
        String cacheKey = "item:solo";

        ItemDto cached = new ItemDto(44L, "From Cache 3", 987, "cached desc", null);

        itemRedisTemplate.opsForHash().put(cacheKey, cached.getId(), cached)
                .then(itemRedisTemplate.expire(cacheKey, Duration.ofSeconds(10)))
                .block();

        StepVerifier.create(itemService.findById(cached.getId()))
                .expectNextMatches(dto -> dto.getName().equals("From Cache 3"))
                .verifyComplete();

    }

}
