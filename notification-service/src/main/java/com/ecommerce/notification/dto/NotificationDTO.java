package com.ecommerce.notification.dto;

import com.ecommerce.notification.enums.NotificationChannel;
import com.ecommerce.notification.enums.NotificationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private String type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private Boolean isRead;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
