package com.kidfashion.ecommerce.kids_fashion_shop;

import com.kidfashion.ecommerce.kids_fashion_shop.config.AdminBootstrapProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class KidsFashionShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(KidsFashionShopApplication.class, args);
	}

}
