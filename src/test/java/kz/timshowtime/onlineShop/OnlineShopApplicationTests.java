package kz.timshowtime.onlineShop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@ImportTestcontainers(TestcontainersConfiguration.class)
@Testcontainers
@SpringBootTest
class OnlineShopApplicationTests {

	@Test
	void contextLoads() {
	}

}
