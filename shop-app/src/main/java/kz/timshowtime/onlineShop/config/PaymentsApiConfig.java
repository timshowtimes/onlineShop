package kz.timshowtime.onlineShop.config;

import kz.timshowtime.onlineShop.paymentsclient.ApiClient;
import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentsApiConfig {

    @Value("${payments.url:http://localhost:8082}")
    private String paymentsUrl;

    @Bean
    public ApiClient apiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(paymentsUrl);
        return apiClient;
    }

    @Bean
    public WalletsApi walletsApi(ApiClient apiClient) {
        return new WalletsApi(apiClient);
    }
}
