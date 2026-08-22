package com.ecommerce.review.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.review.dto.CreateReviewRequest;
import com.ecommerce.review.dto.ProductRatingSummaryDTO;
import com.ecommerce.review.dto.ReviewDTO;
import com.ecommerce.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "Endpoints for Customer Product Reviews, Rating Breakdown, and Helpful Votes")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Add a product review", description = "Submits a customer star rating (1-5) and review comment for a product")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO review = reviewService.addReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Review added successfully"));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Retrieves approved customer reviews for a given product")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDTO>>> getProductReviews(
            @PathVariable("productId") UUID productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ReviewDTO> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Product reviews retrieved successfully"));
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get product rating breakdown summary", description = "Retrieves pre-aggregated average rating, total reviews, and star breakdown (1-5 stars)")
    public ResponseEntity<ApiResponse<ProductRatingSummaryDTO>> getProductRatingSummary(@PathVariable("productId") UUID productId) {
        ProductRatingSummaryDTO summary = reviewService.getProductRatingSummary(productId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Product rating summary retrieved successfully"));
    }

    @PostMapping("/{id}/vote")
    @Operation(summary = "Vote on a review (Helpful / Unhelpful)", description = "Appends a helpful or unhelpful vote to a customer review")
    public ResponseEntity<ApiResponse<ReviewDTO>> voteReview(
            @PathVariable("id") UUID id,
            @RequestParam("userId") UUID userId,
            @RequestParam("isHelpful") boolean isHelpful
    ) {
        ReviewDTO review = reviewService.voteReview(id, userId, isHelpful);
        return ResponseEntity.ok(ApiResponse.success(review, "Review vote recorded successfully"));
    }
}
