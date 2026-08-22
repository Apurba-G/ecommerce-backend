package com.ecommerce.search.dto;

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
public class SearchProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID productId;
    private String name;
    private String slug;
    private String sku;
    private UUID categoryId;
    private String categoryName;
    private String categoryPath;
    private UUID brandId;
    private String brandName;
    private BigDecimal basePrice;
    private BigDecimal sellingPrice;
    private String primaryImage;
    private Boolean isActive;
    private Boolean inStock;
    private Integer totalStock;
    private Double rating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
