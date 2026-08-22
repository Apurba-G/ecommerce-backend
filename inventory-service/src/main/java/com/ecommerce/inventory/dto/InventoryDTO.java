package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID productId;
    private UUID variantId;
    private UUID warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private UUID sellerId;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer soldQuantity;
    private Integer lowStockThreshold;
    private Boolean trackInventory;
    private String batchNumber;
    private LocalDate expiryDate;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
