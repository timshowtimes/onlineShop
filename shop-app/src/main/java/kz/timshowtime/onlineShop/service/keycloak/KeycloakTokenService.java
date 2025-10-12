package kz.timshowtime.onlineShop.service.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
public class KeycloakTokenService {
    private final WebClient webClient;

    @Value("${keycloak.token.url}")
    private String tokenUrl;

    @Value("${keycloak.client.id}")
    private String clientId;

    @Value("${keycloak.client.secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant expiresAt;


    public KeycloakTokenService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Mono<String> getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt)) {
            return Mono.just(cachedToken);
        }

        return webClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String token = String.valueOf(response.get("access_token"));
                    int expiresIn = Integer.parseInt(String.valueOf(response.get("expires_in")));
                    this.cachedToken = token;
                    this.expiresAt = Instant.now().plusSeconds(expiresIn - 30);
                    return token;
                });
    }
}
