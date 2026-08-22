package com.ecommerce.seller.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sellerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Seller Service API")
                        .description("REST API for Seller Profiles, Onboarding KYC Verification, Payouts, and Seller Analytics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("E-Commerce Development Team")
                                .email("dev@ecommerce.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
