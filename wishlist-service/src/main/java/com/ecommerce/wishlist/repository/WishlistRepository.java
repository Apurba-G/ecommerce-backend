package com.ecommerce.wishlist.repository;

import com.ecommerce.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    List<Wishlist> findByUserId(UUID userId);
    Optional<Wishlist> findByUserIdAndIsDefaultTrue(UUID userId);
    Optional<Wishlist> findByShareTokenAndIsPublicTrue(String shareToken);
    Optional<Wishlist> findByUserIdAndName(UUID userId, String name);
    boolean existsByUserIdAndName(UUID userId, String name);
}
