package kz.timshowtime.onlineShop.factory;

import kz.timshowtime.onlineShop.paymentsclient.ApiClient;
import kz.timshowtime.onlineShop.paymentsclient.api.WalletsApi;
import org.springframework.stereotype.Component;

@Component
public class WalletsApiFactory {
    public WalletsApi create() {
        ApiClient apiClient = new ApiClient();
        return new WalletsApi(apiClient);
    }
}
