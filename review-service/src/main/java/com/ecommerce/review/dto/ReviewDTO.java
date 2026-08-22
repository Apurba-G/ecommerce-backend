package com.ecommerce.review.dto;

import com.ecommerce.review.enums.ReviewStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private UUID id;
    private UUID productId;
    private UUID userId;
    private UUID orderId;
    private Integer rating;
    private String title;
    private String body;
    private Boolean verifiedPurchase;
    private Boolean isApproved;
    private Integer helpfulVotes;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
