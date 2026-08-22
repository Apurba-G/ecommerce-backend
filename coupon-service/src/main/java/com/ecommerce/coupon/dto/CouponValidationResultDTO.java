package com.ecommerce.coupon.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResultDTO {
    private Boolean isValid;
    private BigDecimal discountAmount;
    private String message;
    private String couponCode;
}
