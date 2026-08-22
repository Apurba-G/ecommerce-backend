package com.ecommerce.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_rating_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "one_star", nullable = false)
    @Builder.Default
    private Integer oneStar = 0;

    @Column(name = "two_star", nullable = false)
    @Builder.Default
    private Integer twoStar = 0;

    @Column(name = "three_star", nullable = false)
    @Builder.Default
    private Integer threeStar = 0;

    @Column(name = "four_star", nullable = false)
    @Builder.Default
    private Integer fourStar = 0;

    @Column(name = "five_star", nullable = false)
    @Builder.Default
    private Integer fiveStar = 0;

    @Column(name = "verified_reviews", nullable = false)
    @Builder.Default
    private Integer verifiedReviews = 0;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
