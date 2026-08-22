package com.ecommerce.coupon.dto;

import com.ecommerce.coupon.enums.ApplicableFor;
import com.ecommerce.coupon.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotBlank(message = "Coupon name is required")
    private String name;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.00", message = "Discount value cannot be negative")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minimumOrderValue;

    private Integer usageLimit;

    private Integer usageLimitPerUser;

    private Boolean isPublic;

    private UUID sellerId;

    private UUID categoryId;

    private UUID productId;

    private ApplicableFor applicableFor;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;
}
