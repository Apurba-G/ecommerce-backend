package com.ecommerce.user.event;

import com.ecommerce.common.constant.EventConstants;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserProfileRepository userProfileRepository;

    @RabbitListener(queues = EventConstants.USER_PROFILE_QUEUE)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received user.registered event for userId: {}, email: {}", event.userId(), event.email());

        if (userProfileRepository.existsByUserId(event.userId())) {
            log.info("UserProfile already exists for userId: {}", event.userId());
            return;
        }

        UserProfile userProfile = UserProfile.builder()
                .userId(event.userId())
                .firstName(event.firstName())
                .lastName(event.lastName())
                .phone(event.phone())
                .build();

        userProfileRepository.save(userProfile);
        log.info("Successfully created UserProfile for userId: {}", event.userId());
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
