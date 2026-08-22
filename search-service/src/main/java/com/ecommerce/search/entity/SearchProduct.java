package com.ecommerce.search.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RedisHash("search_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProduct implements Serializable {

    @Id
    private String id; // Product UUID string

    @Indexed
    private UUID productId;

    @Indexed
    private String name;

    private String description;
    private String sku;
    private String brandName;
    private String categoryName;

    @Indexed
    private UUID categoryId;

    @Indexed
    private UUID brandId;

    private BigDecimal basePrice;
    private BigDecimal sellingPrice;
    private String primaryImage;

    @Indexed
    private Boolean inStock;

    @Indexed
    private Boolean isActive;

    private Double rating;
    private Integer reviewCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
