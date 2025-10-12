package kz.timshowtime.paymentsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Configuration
@Profile("test")
public class TestJwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return token -> Mono.just(Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("preferred_username", "test_user")
                .claim("realm_access", Map.of("roles", List.of("ROLE_ONLINE_SHOP")))
                .build());
    }
}