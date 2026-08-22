package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private UUID brandId;

    @NotBlank(message = "Product name is required")
    private String name;

    private String shortDescription;
    private String description;
    private String sku;
    private String barcode;

    @NotNull(message = "Base price is required")
    @PositiveOrZero(message = "Base price must be positive or zero")
    private BigDecimal basePrice;

    @NotNull(message = "Selling price is required")
    @PositiveOrZero(message = "Selling price must be positive or zero")
    private BigDecimal sellingPrice;

    private BigDecimal discountPercentage;
    private BigDecimal taxPercentage;
    @Builder.Default
    private String status = "ACTIVE";
    @Builder.Default
    private boolean isFeatured = false;
    @Builder.Default
    private boolean isReturnable = true;
    @Builder.Default
    private Integer returnPeriodDays = 7;

    private List<VariantCreateRequest> variants;
    private List<ImageCreateRequest> images;
    private List<SpecificationCreateRequest> specifications;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantCreateRequest {
        @NotBlank(message = "Variant name is required")
        private String name;
        private String sku;
        private String attributes;
        @NotNull(message = "Price is required")
        private BigDecimal price;
        @NotNull(message = "Selling price is required")
        private BigDecimal sellingPrice;
        private String imageUrl;
        @Builder.Default
        private int sortOrder = 0;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageCreateRequest {
        @NotBlank(message = "Image URL is required")
        private String imageUrl;
        private String altText;
        @Builder.Default
        private boolean isPrimary = false;
        @Builder.Default
        private int sortOrder = 0;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpecificationCreateRequest {
        @NotBlank(message = "Spec key is required")
        private String specKey;
        @NotBlank(message = "Spec value is required")
        private String specValue;
        @Builder.Default
        private String specGroup = "General";
        @Builder.Default
        private int sortOrder = 0;
    }
}
