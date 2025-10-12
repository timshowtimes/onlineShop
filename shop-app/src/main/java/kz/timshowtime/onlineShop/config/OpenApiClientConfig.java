package kz.timshowtime.onlineShop.config;

import kz.timshowtime.onlineShop.service.keycloak.KeycloakTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class OpenApiClientConfig {

    private final KeycloakTokenService keycloakTokenService;

    @Bean
    public WebClient paymentsWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .filter(authorizationFilter())
                .build();
    }

    private ExchangeFilterFunction authorizationFilter() {
        return ((request, next) -> keycloakTokenService.getAccessToken()
                .flatMap(token -> {
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();
                    return next.exchange(newRequest);
                }));
    }
}
