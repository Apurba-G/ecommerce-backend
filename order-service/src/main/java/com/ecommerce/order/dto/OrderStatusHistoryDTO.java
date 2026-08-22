package com.ecommerce.order.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryDTO {
    private UUID id;
    private String fromStatus;
    private String toStatus;
    private String changedBy;
    private String notes;
    private LocalDateTime createdAt;
}
