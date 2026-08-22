package com.ecommerce.coupon.entity;

import com.ecommerce.coupon.enums.RestrictionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "coupon_restrictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false, length = 50)
    private RestrictionType restrictionType;

    @Column(name = "restriction_value", length = 500)
    private String restrictionValue;
}
