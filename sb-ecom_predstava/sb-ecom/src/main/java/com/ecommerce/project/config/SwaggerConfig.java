package com.ecommerce.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme beaterScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer Token");

        SecurityRequirement bearerRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot eCommerce API")
                        .version("1.0")
                        .description("This is a Spring Boot Project for eCommerce")
                        .contact(new Contact()
                                .name("Stjepan Radičev")
                                .email("stjepan.radicev1@gmail.com")
                                .url("https://github.com/StjepanRadicev/web_shop_kazaliste"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", beaterScheme))
                .addSecurityItem(bearerRequirement);
    }
}
