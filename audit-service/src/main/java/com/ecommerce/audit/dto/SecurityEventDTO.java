package com.ecommerce.audit.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventDTO {
    private UUID id;
    private UUID userId;
    private String eventType;
    private String ipAddress;
    private String deviceInfo;
    private Boolean success;
    private String failureReason;
    private LocalDateTime createdAt;
}
