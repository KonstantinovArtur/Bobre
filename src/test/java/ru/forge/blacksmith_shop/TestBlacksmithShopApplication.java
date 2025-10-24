package ru.forge.blacksmith_shop;

import org.springframework.boot.SpringApplication;

public class TestBlacksmithShopApplication {

	public static void main(String[] args) {
		SpringApplication.from(BlacksmithShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
