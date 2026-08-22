package com.ecommerce.coupon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI couponServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coupon Service API")
                        .description("REST API for Voucher Discounts, Usage Restrictions, and PL/pgSQL Validation Engine")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("E-Commerce Development Team")
                                .email("dev@ecommerce.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
