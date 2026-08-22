package com.ecommerce.product.event;

import com.ecommerce.common.constant.EventConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishProductCreated(UUID productId, String name, String slug, String sku) {
        ProductChangedEvent event = new ProductChangedEvent(productId, name, slug, sku, "CREATED", Instant.now());
        log.info("Publishing product.created event for productId: {}", productId);
        rabbitTemplate.convertAndSend(EventConstants.CATALOG_EXCHANGE, EventConstants.PRODUCT_CREATED_KEY, event);
    }

    public void publishProductUpdated(UUID productId, String name, String slug, String sku) {
        ProductChangedEvent event = new ProductChangedEvent(productId, name, slug, sku, "UPDATED", Instant.now());
        log.info("Publishing product.updated event for productId: {}", productId);
        rabbitTemplate.convertAndSend(EventConstants.CATALOG_EXCHANGE, EventConstants.PRODUCT_UPDATED_KEY, event);
    }

    public record ProductChangedEvent(
            UUID productId,
            String name,
            String slug,
            String sku,
            String action,
            Instant timestamp
    ) implements Serializable {}
}
