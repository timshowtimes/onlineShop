package kz.timshowtime.onlineShop;

import org.springframework.boot.SpringApplication;

public class TestOnlineShopApplication {

	public static void main(String[] args) {
		SpringApplication.from(OnlineShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
