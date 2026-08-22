package com.ecommerce.notification.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.notification.dto.NotificationDTO;
import com.ecommerce.notification.dto.SendNotificationRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationDTO sendNotification(SendNotificationRequest request);

    PagedResponse<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable);

    void markAsRead(UUID notificationId);
}
