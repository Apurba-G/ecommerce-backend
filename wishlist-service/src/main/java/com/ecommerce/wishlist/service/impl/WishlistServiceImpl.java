package com.ecommerce.wishlist.service.impl;

import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.wishlist.dto.WishlistCreateRequest;
import com.ecommerce.wishlist.dto.WishlistDTO;
import com.ecommerce.wishlist.dto.WishlistItemAddRequest;
import com.ecommerce.wishlist.dto.WishlistItemDTO;
import com.ecommerce.wishlist.entity.Wishlist;
import com.ecommerce.wishlist.entity.WishlistItem;
import com.ecommerce.wishlist.repository.WishlistItemRepository;
import com.ecommerce.wishlist.repository.WishlistRepository;
import com.ecommerce.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    private WishlistItemDTO mapItemToDTO(WishlistItem item) {
        return WishlistItemDTO.builder()
                .id(item.getId())
                .wishlistId(item.getWishlist().getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .inStock(item.getInStock())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private WishlistDTO mapToDTO(Wishlist w) {
        List<WishlistItemDTO> items = w.getItems() != null
                ? w.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList())
                : List.of();

        return WishlistDTO.builder()
                .id(w.getId())
                .userId(w.getUserId())
                .name(w.getName())
                .isDefault(w.getIsDefault())
                .isPublic(w.getIsPublic())
                .shareToken(w.getShareToken())
                .items(items)
                .itemCount(items.size())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", allEntries = true)
    public WishlistDTO createWishlist(UUID userId, WishlistCreateRequest request) {
        if (wishlistRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE, "A wishlist named '" + request.getName() + "' already exists");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            wishlistRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(w -> {
                w.setIsDefault(false);
                wishlistRepository.save(w);
            });
        }

        String shareToken = Boolean.TRUE.equals(request.getIsPublic())
                ? "wishlist-" + UUID.randomUUID().toString().substring(0, 8)
                : null;

        Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .name(request.getName().trim())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .shareToken(shareToken)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Created wishlist '{}' for user {}", saved.getName(), userId);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "wishlist", key = "#userId.toString() + '-' + #wishlistId.toString()")
    public WishlistDTO getWishlistById(UUID userId, UUID wishlistId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "id", wishlistId));

        if (!wishlist.getUserId().equals(userId) && !Boolean.TRUE.equals(wishlist.getIsPublic())) {
            throw new UnauthorizedException("You do not have access to this wishlist");
        }

        return mapToDTO(wishlist);
    }

    @Override
    @Transactional
    public WishlistDTO getDefaultWishlist(UUID userId) {
        Wishlist defaultList = wishlistRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    Wishlist created = Wishlist.builder()
                            .userId(userId)
                            .name("My Favorites")
                            .isDefault(true)
                            .isPublic(false)
                            .build();
                    return wishlistRepository.save(created);
                });
        return mapToDTO(defaultList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistDTO> getUserWishlists(UUID userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistDTO getPublicWishlistByShareToken(String shareToken) {
        Wishlist wishlist = wishlistRepository.findByShareTokenAndIsPublicTrue(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Public wishlist not found for token: " + shareToken));
        return mapToDTO(wishlist);
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", allEntries = true)
    public WishlistDTO addItemToWishlist(UUID userId, UUID wishlistId, WishlistItemAddRequest request) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "id", wishlistId));

        if (!wishlist.getUserId().equals(userId)) {
            throw new UnauthorizedException("You cannot modify another user's wishlist");
        }

        Optional<WishlistItem> existing = request.getVariantId() != null
                ? wishlistItemRepository.findByWishlistIdAndProductIdAndVariantId(wishlistId, request.getProductId(), request.getVariantId())
                : wishlistItemRepository.findByWishlistIdAndProductIdAndVariantIdIsNull(wishlistId, request.getProductId());

        if (existing.isPresent()) {
            // Already in wishlist, return as is
            return mapToDTO(wishlist);
        }

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .productName(request.getProductName())
                .productImage(request.getProductImage())
                .price(request.getPrice())
                .inStock(request.getInStock() != null ? request.getInStock() : true)
                .build();

        wishlist.getItems().add(item);
        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Added item '{}' to wishlist id {}", request.getProductName(), wishlistId);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", allEntries = true)
    public WishlistDTO addItemToDefaultWishlist(UUID userId, WishlistItemAddRequest request) {
        Wishlist defaultWishlist = wishlistRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    Wishlist created = Wishlist.builder()
                            .userId(userId)
                            .name("My Favorites")
                            .isDefault(true)
                            .isPublic(false)
                            .build();
                    return wishlistRepository.save(created);
                });

        return addItemToWishlist(userId, defaultWishlist.getId(), request);
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", allEntries = true)
    public void removeItemFromWishlist(UUID userId, UUID wishlistId, UUID itemId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "id", wishlistId));

        if (!wishlist.getUserId().equals(userId)) {
            throw new UnauthorizedException("You cannot modify another user's wishlist");
        }

        wishlist.getItems().removeIf(item -> item.getId().equals(itemId));
        wishlistRepository.save(wishlist);
        log.info("Removed item id {} from wishlist id {}", itemId, wishlistId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "wishlist", allEntries = true)
    public void deleteWishlist(UUID userId, UUID wishlistId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "id", wishlistId));

        if (!wishlist.getUserId().equals(userId)) {
            throw new UnauthorizedException("You cannot delete another user's wishlist");
        }

        wishlistRepository.delete(wishlist);
        log.info("Deleted wishlist id {}", wishlistId);
    }
}
