package com.ecommerce.coupon.dto;

import com.ecommerce.coupon.enums.ApplicableFor;
import com.ecommerce.coupon.enums.DiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minimumOrderValue;
    private Integer usageLimit;
    private Integer usageLimitPerUser;
    private Integer usedCount;
    private Boolean isActive;
    private Boolean isPublic;
    private UUID sellerId;
    private UUID categoryId;
    private UUID productId;
    private ApplicableFor applicableFor;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
