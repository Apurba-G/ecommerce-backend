package com.ecommerce.coupon.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.coupon.dto.CouponDTO;
import com.ecommerce.coupon.dto.CouponValidationResultDTO;
import com.ecommerce.coupon.dto.CreateCouponRequest;
import com.ecommerce.coupon.dto.ValidateCouponRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CouponService {

    CouponDTO createCoupon(CreateCouponRequest request);

    CouponDTO getCouponByCode(String code);

    PagedResponse<CouponDTO> getActivePublicCoupons(Pageable pageable);

    CouponValidationResultDTO validateCoupon(ValidateCouponRequest request);

    void recordCouponUsage(String code, UUID userId, UUID orderId, java.math.BigDecimal discountApplied);
}
