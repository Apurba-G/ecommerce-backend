package com.ecommerce.wishlist.dto;

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
public class WishlistItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID wishlistId;
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Boolean inStock;
    private LocalDateTime createdAt;
}
