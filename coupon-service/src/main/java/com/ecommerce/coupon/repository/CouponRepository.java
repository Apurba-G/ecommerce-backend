package com.ecommerce.coupon.repository;

import com.ecommerce.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCode(String code);

    Page<Coupon> findByIsActiveTrueAndIsPublicTrue(Pageable pageable);

    List<Coupon> findBySellerId(UUID sellerId);

    @Query(value = "SELECT * FROM validate_coupon(:code, :userId, :orderAmount, :orderId)", nativeQuery = true)
    List<Object[]> executeValidateCouponProcedure(
            @Param("code") String code,
            @Param("userId") UUID userId,
            @Param("orderAmount") BigDecimal orderAmount,
            @Param("orderId") UUID orderId
    );
}
