package com.ecommerce.coupon.repository;

import com.ecommerce.coupon.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    List<CouponUsage> findByUserId(UUID userId);

    long countByCouponIdAndUserId(UUID couponId, UUID userId);
}
