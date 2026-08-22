package com.ecommerce.order.saga;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.enums.OutboxStatus;
import com.ecommerce.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void startOrderCheckoutSaga(Order order) {
        log.info("Starting Order Checkout Saga for orderNumber: {}", order.getOrderNumber());
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", order.getId());
            payload.put("orderNumber", order.getOrderNumber());
            payload.put("userId", order.getUserId());
            payload.put("totalAmount", order.getTotalAmount());
            payload.put("status", order.getOrderStatus().name());

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .eventType("OrderCreatedEvent")
                    .payload(objectMapper.writeValueAsString(payload))
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("OrderCreatedEvent transactional outbox record saved for orderId: {}", order.getId());

        } catch (Exception e) {
            log.error("Failed to serialize outbox event for orderId: {}", order.getId(), e);
        }
    }

    public void compensateOrderFailure(Order order, String reason) {
        log.warn("Triggering compensating transaction for orderId: {}, reason: {}", order.getId(), reason);
        order.setOrderStatus(OrderStatus.FAILED);
        order.setCancellationReason(reason);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", order.getId());
            payload.put("orderNumber", order.getOrderNumber());
            payload.put("reason", reason);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .eventType("OrderCancelledEvent")
                    .payload(objectMapper.writeValueAsString(payload))
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize compensating outbox event for orderId: {}", order.getId(), e);
        }
    }
}
