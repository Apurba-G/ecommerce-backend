package com.ecommerce.coupon.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.coupon.dto.CouponDTO;
import com.ecommerce.coupon.dto.CouponValidationResultDTO;
import com.ecommerce.coupon.dto.CreateCouponRequest;
import com.ecommerce.coupon.dto.ValidateCouponRequest;
import com.ecommerce.coupon.entity.Coupon;
import com.ecommerce.coupon.entity.CouponUsage;
import com.ecommerce.coupon.enums.ApplicableFor;
import com.ecommerce.coupon.repository.CouponRepository;
import com.ecommerce.coupon.repository.CouponUsageRepository;
import com.ecommerce.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponDTO createCoupon(CreateCouponRequest request) {
        log.info("Creating new coupon with code: {}", request.getCode());

        String formattedCode = request.getCode().trim().toUpperCase();

        if (couponRepository.findByCode(formattedCode).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists: " + formattedCode);
        }

        Coupon coupon = Coupon.builder()
                .code(formattedCode)
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minimumOrderValue(request.getMinimumOrderValue() != null ? request.getMinimumOrderValue() : BigDecimal.ZERO)
                .usageLimit(request.getUsageLimit())
                .usageLimitPerUser(request.getUsageLimitPerUser() != null ? request.getUsageLimitPerUser() : 1)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .sellerId(request.getSellerId())
                .categoryId(request.getCategoryId())
                .productId(request.getProductId())
                .applicableFor(request.getApplicableFor() != null ? request.getApplicableFor() : ApplicableFor.ALL)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();

        Coupon saved = couponRepository.save(coupon);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "coupons", key = "#code")
    public CouponDTO getCouponByCode(String code) {
        String formattedCode = code.trim().toUpperCase();
        Coupon coupon = couponRepository.findByCode(formattedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", formattedCode));
        return mapToDTO(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CouponDTO> getActivePublicCoupons(Pageable pageable) {
        Page<Coupon> page = couponRepository.findByIsActiveTrueAndIsPublicTrue(pageable);
        List<CouponDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<CouponDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResultDTO validateCoupon(ValidateCouponRequest request) {
        String formattedCode = request.getCode().trim().toUpperCase();
        log.info("Validating coupon code: {} for userId: {}, amount: {}", formattedCode, request.getUserId(), request.getOrderAmount());

        try {
            List<Object[]> results = couponRepository.executeValidateCouponProcedure(
                    formattedCode,
                    request.getUserId(),
                    request.getOrderAmount(),
                    request.getOrderId()
            );

            if (results != null && !results.isEmpty()) {
                Object[] row = results.get(0);
                Boolean isValid = (Boolean) row[0];
                BigDecimal discount = (BigDecimal) row[1];
                String message = (String) row[2];

                return CouponValidationResultDTO.builder()
                        .isValid(isValid)
                        .discountAmount(discount != null ? discount : BigDecimal.ZERO)
                        .message(message)
                        .couponCode(formattedCode)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Stored procedure validation fallback for code: {}", formattedCode, e);
        }

        // Fallback Java-level evaluation
        Coupon coupon = couponRepository.findByCode(formattedCode).orElse(null);
        if (coupon == null || !coupon.getIsActive()) {
            return CouponValidationResultDTO.builder().isValid(false).discountAmount(BigDecimal.ZERO).message("Invalid coupon code").couponCode(formattedCode).build();
        }

        if (request.getOrderAmount().compareTo(coupon.getMinimumOrderValue()) < 0) {
            return CouponValidationResultDTO.builder().isValid(false).discountAmount(BigDecimal.ZERO).message("Minimum order value required: " + coupon.getMinimumOrderValue()).couponCode(formattedCode).build();
        }

        BigDecimal discount = coupon.getDiscountValue();
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        return CouponValidationResultDTO.builder()
                .isValid(true)
                .discountAmount(discount)
                .message("Coupon applied successfully")
                .couponCode(formattedCode)
                .build();
    }

    @Override
    @Transactional
    public void recordCouponUsage(String code, UUID userId, UUID orderId, BigDecimal discountApplied) {
        String formattedCode = code.trim().toUpperCase();
        Coupon coupon = couponRepository.findByCode(formattedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", formattedCode));

        coupon.setUsedCount(coupon.getUsedCount() + 1);

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .userId(userId)
                .orderId(orderId)
                .discountApplied(discountApplied)
                .build();

        couponUsageRepository.save(usage);
        couponRepository.save(coupon);
        log.info("Recorded usage of coupon: {} for orderId: {}", formattedCode, orderId);
    }

    private CouponDTO mapToDTO(Coupon coupon) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minimumOrderValue(coupon.getMinimumOrderValue())
                .usageLimit(coupon.getUsageLimit())
                .usageLimitPerUser(coupon.getUsageLimitPerUser())
                .usedCount(coupon.getUsedCount())
                .isActive(coupon.getIsActive())
                .isPublic(coupon.getIsPublic())
                .sellerId(coupon.getSellerId())
                .categoryId(coupon.getCategoryId())
                .productId(coupon.getProductId())
                .applicableFor(coupon.getApplicableFor())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
