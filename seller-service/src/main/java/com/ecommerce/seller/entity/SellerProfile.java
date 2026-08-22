package com.ecommerce.seller.entity;

import com.ecommerce.seller.enums.BusinessType;
import com.ecommerce.seller.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "business_name", nullable = false, unique = true, length = 300)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 100)
    private BusinessType businessType;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "business_address", length = 500)
    private String businessAddress;

    @Column(name = "business_city", length = 100)
    private String businessCity;

    @Column(name = "business_state", length = 100)
    private String businessState;

    @Column(name = "business_country", length = 100)
    @Builder.Default
    private String businessCountry = "India";

    @Column(name = "business_postal_code", length = 20)
    private String businessPostalCode;

    @Column(name = "bank_account_number", length = 20)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc", length = 11)
    private String bankIfsc;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "bank_account_holder", length = 200)
    private String bankAccountHolder;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status", nullable = false, length = 30)
    @Builder.Default
    private SellerStatus sellerStatus = SellerStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal commissionRate = BigDecimal.valueOf(10.00);

    @Column(name = "total_revenue", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_payouts", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPayouts = BigDecimal.ZERO;

    @Column(name = "pending_payout", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pendingPayout = BigDecimal.ZERO;

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_products", nullable = false)
    @Builder.Default
    private Integer totalProducts = 0;

    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "return_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal returnRate = BigDecimal.ZERO;

    @Column(name = "cancellation_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cancellationRate = BigDecimal.ZERO;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SellerDocument> documents = new ArrayList<>();
}
