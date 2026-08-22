package com.ecommerce.audit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEventRequest {

    private UUID userId;

    @NotBlank(message = "Event type is required (e.g. LOGIN_SUCCESS, LOGIN_FAILED)")
    private String eventType;

    private String ipAddress;

    private String deviceInfo;

    private Boolean success;

    private String failureReason;
}
