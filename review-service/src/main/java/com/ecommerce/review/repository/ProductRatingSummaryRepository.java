package com.ecommerce.review.repository;

import com.ecommerce.review.entity.ProductRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRatingSummaryRepository extends JpaRepository<ProductRatingSummary, UUID> {

    Optional<ProductRatingSummary> findByProductId(UUID productId);
}
