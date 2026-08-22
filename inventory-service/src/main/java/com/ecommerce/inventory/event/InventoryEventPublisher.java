package com.ecommerce.inventory.event;

import com.ecommerce.inventory.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockUpdated(UUID productId, UUID variantId, int availableQuantity) {
        Map<String, Object> payload = Map.of(
                "productId", productId.toString(),
                "variantId", variantId != null ? variantId.toString() : "",
                "availableQuantity", availableQuantity,
                "inStock", availableQuantity > 0
        );
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE, RabbitMQConfig.STOCK_UPDATED_ROUTING_KEY, payload);
            log.info("Published stock updated event for product {}: available {}", productId, availableQuantity);

            if (availableQuantity <= 0) {
                rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE, RabbitMQConfig.OUT_OF_STOCK_ROUTING_KEY, payload);
                log.warn("Published OUT OF STOCK event for product {}", productId);
            }
        } catch (Exception e) {
            log.error("Failed to publish inventory event for product {}: {}", productId, e.getMessage());
        }
    }
}
