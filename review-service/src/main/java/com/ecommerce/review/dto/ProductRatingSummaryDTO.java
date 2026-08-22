package com.ecommerce.review.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummaryDTO {
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer oneStar;
    private Integer twoStar;
    private Integer threeStar;
    private Integer fourStar;
    private Integer fiveStar;
    private Integer verifiedReviews;
}
