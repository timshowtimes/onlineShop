package kz.timshowtime.onlineShop.factory;

import kz.timshowtime.onlineShop.paymentsclient.ApiClient;
import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WalletsApiFactory {
    private final String baseUrl;

    public WalletsApiFactory(@Value("${payments.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public WalletsApi create() {
        System.out.println("WalletsApi baseUrl = " + baseUrl);
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        return new WalletsApi(apiClient);
    }
}
