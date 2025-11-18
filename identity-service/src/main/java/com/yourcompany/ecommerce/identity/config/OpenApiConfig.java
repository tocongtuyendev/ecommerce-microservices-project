package com.yourcompany.ecommerce.identity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Identity Service API")
                        .version("v1")
                        .description("Authentication and Authorization API for ecommerce microservices")
                        .contact(new Contact().name("YourCompany").email("dev@yourcompany.com")));
    }
}
