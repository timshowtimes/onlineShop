package kz.timshowtime.onlineShop.factory;

import kz.timshowtime.onlineShop.paymentsclient.ApiClient;
import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WalletsApiFactory {
    private final WebClient paymentsWebClient;

    @Value("${payments.url}")
    private String paymentsUrl;

    public WalletsApiFactory(WebClient paymentsWebClient) {
        this.paymentsWebClient = paymentsWebClient;
    }

    public WalletsApi create() {
        ApiClient apiClient = new ApiClient(paymentsWebClient);
        apiClient.setBasePath(paymentsUrl);
        return new WalletsApi(apiClient);
    }
}
