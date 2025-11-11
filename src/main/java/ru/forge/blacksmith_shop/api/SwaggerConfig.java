package ru.forge.blacksmith_shop.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI blacksmithOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blacksmith Shop API")
                        .version("v1.0")
                        .description("API для работы с товарами кузницы и другими модулями системы"));
    }
}
