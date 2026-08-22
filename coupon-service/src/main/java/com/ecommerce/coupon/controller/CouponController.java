package com.ecommerce.coupon.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.coupon.dto.CouponDTO;
import com.ecommerce.coupon.dto.CouponValidationResultDTO;
import com.ecommerce.coupon.dto.CreateCouponRequest;
import com.ecommerce.coupon.dto.ValidateCouponRequest;
import com.ecommerce.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Controller", description = "Endpoints for Discount Vouchers, PL/pgSQL Coupon Engine, and Validation")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Create a new coupon", description = "Creates a voucher discount with minimum order value and usage limits")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponDTO coupon = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(coupon, "Coupon created successfully"));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by Code", description = "Retrieves coupon details by code e.g. WELCOME10")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponByCode(@PathVariable("code") String code) {
        CouponDTO coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(ApiResponse.success(coupon, "Coupon retrieved successfully"));
    }

    @GetMapping("/public")
    @Operation(summary = "List active public coupons", description = "Retrieves store-wide active discount vouchers available for customers")
    public ResponseEntity<ApiResponse<PagedResponse<CouponDTO>>> getActivePublicCoupons(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<CouponDTO> coupons = couponService.getActivePublicCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success(coupons, "Public coupons retrieved successfully"));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate & calculate coupon discount", description = "Evaluates voucher eligibility and calculates net savings")
    public ResponseEntity<ApiResponse<CouponValidationResultDTO>> validateCoupon(@Valid @RequestBody ValidateCouponRequest request) {
        CouponValidationResultDTO result = couponService.validateCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Coupon evaluated successfully"));
    }
}
