package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAlertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID inventoryId;
    private UUID productId;
    private String alertType;
    private Integer thresholdQuantity;
    private Integer currentQuantity;
    private Boolean isResolved;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
