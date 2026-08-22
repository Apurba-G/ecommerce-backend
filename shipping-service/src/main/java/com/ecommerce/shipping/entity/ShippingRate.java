package com.ecommerce.shipping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipping_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private ShippingZone zone;

    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName;

    @Column(name = "carrier", nullable = false, length = 100)
    private String carrier;

    @Column(name = "base_rate", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal baseRate = BigDecimal.ZERO;

    @Column(name = "per_kg_rate", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal perKgRate = BigDecimal.ZERO;

    @Column(name = "min_weight", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal minWeight = BigDecimal.ZERO;

    @Column(name = "max_weight", precision = 10, scale = 3)
    private BigDecimal maxWeight;

    @Column(name = "estimated_days_min", nullable = false)
    @Builder.Default
    private Integer estimatedDaysMin = 1;

    @Column(name = "estimated_days_max", nullable = false)
    @Builder.Default
    private Integer estimatedDaysMax = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
