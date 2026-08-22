package com.ecommerce.notification.consumer;

import com.ecommerce.notification.config.RabbitMQConfig;
import com.ecommerce.notification.dto.SendNotificationRequest;
import com.ecommerce.notification.enums.NotificationChannel;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotificationEvents(Map<String, Object> event) {
        try {
            log.info("Notification service consumed event from queue: {}", event);

            String userIdStr = (String) event.get("userId");
            if (userIdStr == null) return;

            UUID userId = UUID.fromString(userIdStr);
            String title = (String) event.getOrDefault("title", "Order Notification");
            String message = (String) event.getOrDefault("message", "You have a new update regarding your order.");
            String type = (String) event.getOrDefault("type", "ORDER_UPDATE");

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .userId(userId)
                    .type(type)
                    .channel(NotificationChannel.EMAIL)
                    .title(title)
                    .message(message)
                    .build();

            notificationService.sendNotification(request);
            log.info("Dispatched notification for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error consuming notification event: {}", e.getMessage(), e);
        }
    }
}
