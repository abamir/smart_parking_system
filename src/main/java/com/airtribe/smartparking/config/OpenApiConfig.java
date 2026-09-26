package com.airtribe.smartparking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartParkingOpenAPI() {

        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Smart Parking Management API")
                        .version("1.0.0")
                        .description("REST APIs for managing parking availability, " +
                                "vehicle check-in, vehicle check-out and payments.")
                        .contact(
                                new Contact().name("Smart Parking Development Team")
                        )

                );

    }
}
