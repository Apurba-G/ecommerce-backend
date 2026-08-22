package com.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID cartId;
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productImage;
    private BigDecimal unitPrice;
    private BigDecimal sellingPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String productSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
