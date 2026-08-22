package com.ecommerce.review.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.review.dto.CreateReviewRequest;
import com.ecommerce.review.dto.ProductRatingSummaryDTO;
import com.ecommerce.review.dto.ReviewDTO;
import com.ecommerce.review.entity.ProductRatingSummary;
import com.ecommerce.review.entity.Review;
import com.ecommerce.review.enums.ReviewStatus;
import com.ecommerce.review.repository.ProductRatingSummaryRepository;
import com.ecommerce.review.repository.ReviewRepository;
import com.ecommerce.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRatingSummaryRepository ratingSummaryRepository;

    @Override
    @Transactional
    public ReviewDTO addReview(CreateReviewRequest request) {
        log.info("Adding review for productId: {}, userId: {}", request.getProductId(), request.getUserId());

        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), request.getUserId())) {
            throw new IllegalArgumentException("User has already reviewed this product.");
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .orderItemId(request.getOrderItemId())
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .verifiedPurchase(request.getOrderId() != null)
                .status(ReviewStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review saved with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewDTO> getProductReviews(UUID productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable);
        List<ReviewDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<ReviewDTO>builder()
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
    public ProductRatingSummaryDTO getProductRatingSummary(UUID productId) {
        ProductRatingSummary summary = ratingSummaryRepository.findByProductId(productId)
                .orElse(ProductRatingSummary.builder()
                        .productId(productId)
                        .averageRating(BigDecimal.ZERO)
                        .totalReviews(0)
                        .oneStar(0).twoStar(0).threeStar(0).fourStar(0).fiveStar(0)
                        .verifiedReviews(0)
                        .build());

        return ProductRatingSummaryDTO.builder()
                .averageRating(summary.getAverageRating())
                .totalReviews(summary.getTotalReviews())
                .oneStar(summary.getOneStar())
                .twoStar(summary.getTwoStar())
                .threeStar(summary.getThreeStar())
                .fourStar(summary.getFourStar())
                .fiveStar(summary.getFiveStar())
                .verifiedReviews(summary.getVerifiedReviews())
                .build();
    }

    @Override
    @Transactional
    public ReviewDTO voteReview(UUID reviewId, UUID userId, boolean isHelpful) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId.toString()));

        if (isHelpful) {
            review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        } else {
            review.setUnhelpfulVotes(review.getUnhelpfulVotes() + 1);
        }

        Review updated = reviewRepository.save(review);
        return mapToDTO(updated);
    }

    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .orderId(review.getOrderId())
                .rating(review.getRating())
                .title(review.getTitle())
                .body(review.getBody())
                .verifiedPurchase(review.getVerifiedPurchase())
                .isApproved(review.getIsApproved())
                .helpfulVotes(review.getHelpfulVotes())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
