package com.ecommerce.shipping.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentTrackingDTO {
    private UUID id;
    private String status;
    private String location;
    private String description;
    private String activityCode;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
}
