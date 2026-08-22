package com.ecommerce.brand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.ecommerce.brand", "com.ecommerce.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
public class BrandApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrandApplication.class, args);
    }
}
