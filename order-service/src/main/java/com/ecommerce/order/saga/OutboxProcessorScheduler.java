package com.ecommerce.order.saga;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.enums.OutboxStatus;
import com.ecommerce.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessorScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 3000)
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, 50)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                event.setStatus(OutboxStatus.PROCESSING);
                outboxEventRepository.save(event);

                String routingKey = "OrderCreatedEvent".equalsIgnoreCase(event.getEventType())
                        ? RabbitMQConfig.ORDER_ROUTING_KEY_CREATED
                        : RabbitMQConfig.ORDER_ROUTING_KEY_CANCELLED;

                rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, routingKey, event.getPayload());

                event.setStatus(OutboxStatus.SENT);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("Successfully dispatched outbox event ID: {} [type={}]", event.getId(), event.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish outbox event ID: {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                } else {
                    event.setStatus(OutboxStatus.PENDING);
                }
                outboxEventRepository.save(event);
            }
        }
    }
}
