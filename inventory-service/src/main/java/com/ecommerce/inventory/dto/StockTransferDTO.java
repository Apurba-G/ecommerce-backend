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
public class StockTransferDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID fromWarehouseId;
    private String fromWarehouseName;
    private UUID toWarehouseId;
    private String toWarehouseName;
    private UUID productId;
    private UUID variantId;
    private Integer quantity;
    private String status;
    private String notes;
    private UUID initiatedBy;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
