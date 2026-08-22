package com.ecommerce.wishlist.repository;

import com.ecommerce.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    List<WishlistItem> findByWishlistId(UUID wishlistId);
    Optional<WishlistItem> findByWishlistIdAndProductIdAndVariantIdIsNull(UUID wishlistId, UUID productId);
    Optional<WishlistItem> findByWishlistIdAndProductIdAndVariantId(UUID wishlistId, UUID productId, UUID variantId);
    void deleteByWishlistIdAndProductId(UUID wishlistId, UUID productId);
}
