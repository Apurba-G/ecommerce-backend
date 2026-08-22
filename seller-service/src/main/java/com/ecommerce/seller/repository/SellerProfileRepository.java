package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.SellerProfile;
import com.ecommerce.seller.enums.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, UUID> {

    Optional<SellerProfile> findByUserId(UUID userId);

    Optional<SellerProfile> findByBusinessName(String businessName);

    Page<SellerProfile> findBySellerStatus(SellerStatus status, Pageable pageable);
}
