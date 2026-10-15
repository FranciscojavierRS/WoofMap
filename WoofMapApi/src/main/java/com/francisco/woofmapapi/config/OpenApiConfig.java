package com.francisco.woofmapapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI woofMapOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WoofMap API")
                        .description("API REST para la plataforma colaborativa de lugares dog-friendly WoofMap.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("WoofMap Team")
                                .email("contacto@woofmap.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
