package com.ecommerce.search.consumer;

import com.ecommerce.search.config.RabbitMQConfig;
import com.ecommerce.search.entity.SearchProduct;
import com.ecommerce.search.repository.SearchProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final SearchProductRepository searchProductRepository;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_CATALOG_QUEUE)
    @CacheEvict(value = "search", allEntries = true)
    public void consumeProductEvents(Map<String, Object> event) {
        try {
            log.info("Search service consumed catalog event: {}", event);
            String productIdStr = (String) event.get("productId");
            if (productIdStr == null) return;

            UUID productId = UUID.fromString(productIdStr);
            String eventType = (String) event.getOrDefault("eventType", "PRODUCT_UPDATED");

            if ("PRODUCT_DELETED".equalsIgnoreCase(eventType)) {
                searchProductRepository.deleteById(productId.toString());
                log.info("Evicted product {} from Redis search index", productId);
                return;
            }

            SearchProduct searchProduct = searchProductRepository.findById(productId.toString())
                    .orElseGet(() -> SearchProduct.builder()
                            .id(productId.toString())
                            .productId(productId)
                            .isActive(true)
                            .inStock(true)
                            .build());

            if (event.containsKey("name")) searchProduct.setName((String) event.get("name"));
            if (event.containsKey("description")) searchProduct.setDescription((String) event.get("description"));
            if (event.containsKey("sku")) searchProduct.setSku((String) event.get("sku"));
            if (event.containsKey("categoryName")) searchProduct.setCategoryName((String) event.get("categoryName"));
            if (event.containsKey("brandName")) searchProduct.setBrandName((String) event.get("brandName"));
            if (event.containsKey("primaryImage")) searchProduct.setPrimaryImage((String) event.get("primaryImage"));

            if (event.containsKey("sellingPrice")) {
                Object priceObj = event.get("sellingPrice");
                searchProduct.setSellingPrice(new BigDecimal(priceObj.toString()));
            }
            if (event.containsKey("basePrice")) {
                Object basePriceObj = event.get("basePrice");
                searchProduct.setBasePrice(new BigDecimal(basePriceObj.toString()));
            }
            if (event.containsKey("isActive")) {
                searchProduct.setIsActive((Boolean) event.get("isActive"));
            }
            if (event.containsKey("inStock")) {
                searchProduct.setInStock((Boolean) event.get("inStock"));
            }

            searchProductRepository.save(searchProduct);
            log.info("Updated Redis search index projection for product {}", productId);
        } catch (Exception e) {
            log.error("Error processing catalog event in Search Service: {}", e.getMessage(), e);
        }
    }
}
