package com.ecommerce.product.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String sku;
    private String attributes;
    private BigDecimal price;
    private BigDecimal sellingPrice;
    private String imageUrl;
    private boolean isActive;
    private int sortOrder;
}
