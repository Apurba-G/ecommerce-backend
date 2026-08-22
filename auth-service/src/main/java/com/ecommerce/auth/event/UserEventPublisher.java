package com.ecommerce.auth.event;

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
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UUID userId, String email, String firstName, String lastName, String phone) {
        UserRegisteredEvent event = new UserRegisteredEvent(userId, email, firstName, lastName, phone, Instant.now());
        log.info("Publishing user.registered event for userId: {}", userId);
        rabbitTemplate.convertAndSend(EventConstants.AUTH_EXCHANGE, EventConstants.USER_REGISTERED_KEY, event);
    }

    public record UserRegisteredEvent(
            UUID userId,
            String email,
            String firstName,
            String lastName,
            String phone,
            Instant registeredAt
    ) implements Serializable {}
}
