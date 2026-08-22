package com.ecommerce.audit.consumer;

import com.ecommerce.audit.config.RabbitMQConfig;
import com.ecommerce.audit.dto.LogEventRequest;
import com.ecommerce.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditService auditService;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void consumeAuditEvents(Map<String, Object> event) {
        try {
            log.info("Audit service consumed audit event: {}", event);

            String userIdStr = (String) event.get("userId");
            UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;
            String eventType = (String) event.getOrDefault("eventType", "SYSTEM_AUDIT");
            String ipAddress = (String) event.get("ipAddress");
            String deviceInfo = (String) event.get("deviceInfo");

            LogEventRequest request = LogEventRequest.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .success(true)
                    .build();

            auditService.logSecurityEvent(request);
            log.info("Persisted audit event type: {}", eventType);
        } catch (Exception e) {
            log.error("Error consuming audit event: {}", e.getMessage(), e);
        }
    }
}
