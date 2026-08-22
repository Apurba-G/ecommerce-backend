package com.ecommerce.review.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.review.dto.CreateReviewRequest;
import com.ecommerce.review.dto.ProductRatingSummaryDTO;
import com.ecommerce.review.dto.ReviewDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    ReviewDTO addReview(CreateReviewRequest request);

    PagedResponse<ReviewDTO> getProductReviews(UUID productId, Pageable pageable);

    ProductRatingSummaryDTO getProductRatingSummary(UUID productId);

    ReviewDTO voteReview(UUID reviewId, UUID userId, boolean isHelpful);
}
