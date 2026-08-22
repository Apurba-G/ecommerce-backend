package com.ecommerce.product.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID categoryId;
    private UUID brandId;
    private UUID sellerId;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String sku;
    private String barcode;
    private BigDecimal basePrice;
    private BigDecimal sellingPrice;
    private BigDecimal discountPercentage;
    private BigDecimal taxPercentage;
    private String status;
    private boolean isFeatured;
    private boolean isActive;
    private boolean isReturnable;
    private Integer returnPeriodDays;
    private int viewCount;
    private BigDecimal averageRating;
    private int reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductVariantDTO> variants;
    private List<ProductImageDTO> images;
    private List<ProductSpecificationDTO> specifications;
}
