package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReserveRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    private UUID variantId;

    private UUID warehouseId;

    @NotNull(message = "Quantity to reserve is required")
    @Min(value = 1, message = "Reserved quantity must be at least 1")
    private Integer quantity;

    private String referenceId; // Order ID / Cart ID
}
