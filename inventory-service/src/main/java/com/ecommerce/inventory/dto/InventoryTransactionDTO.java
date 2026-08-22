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
public class InventoryTransactionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID inventoryId;
    private String transactionType;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String referenceType;
    private String referenceId;
    private String note;
    private UUID performedBy;
    private LocalDateTime createdAt;
}
