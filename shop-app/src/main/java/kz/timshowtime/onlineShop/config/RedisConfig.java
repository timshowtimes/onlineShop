package kz.timshowtime.onlineShop.config;

import kz.timshowtime.onlineShop.dto.ItemDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;


@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, ItemDto> cartItemRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<ItemDto> valueSerializer = new Jackson2JsonRedisSerializer<>(ItemDto.class);

        RedisSerializationContext<String, ItemDto> context =
                RedisSerializationContext.<String, ItemDto>newSerializationContext(new StringRedisSerializer())
                        .hashKey(new GenericToStringSerializer<>(Long.class))
                        .hashValue(valueSerializer)
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> pageStringRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        StringRedisSerializer valueSerializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> context =
                RedisSerializationContext
                        .<String, String>newSerializationContext(keySerializer)
                        .key(keySerializer)
                        .value(valueSerializer)
                        .hashKey(keySerializer)
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }





}
