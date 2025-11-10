package ru.forge.blacksmith_shop.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Blacksmith Shop API")
                .version("v1")
                .description("Простой CRUD для витрины и заказов"));
    }
}
