package com.ecommerce.wishlist.service;

import com.ecommerce.wishlist.dto.WishlistCreateRequest;
import com.ecommerce.wishlist.dto.WishlistDTO;
import com.ecommerce.wishlist.dto.WishlistItemAddRequest;
import com.ecommerce.wishlist.dto.WishlistItemDTO;

import java.util.List;
import java.util.UUID;

public interface WishlistService {
    WishlistDTO createWishlist(UUID userId, WishlistCreateRequest request);
    WishlistDTO getWishlistById(UUID userId, UUID wishlistId);
    WishlistDTO getDefaultWishlist(UUID userId);
    List<WishlistDTO> getUserWishlists(UUID userId);
    WishlistDTO getPublicWishlistByShareToken(String shareToken);
    WishlistDTO addItemToWishlist(UUID userId, UUID wishlistId, WishlistItemAddRequest request);
    WishlistDTO addItemToDefaultWishlist(UUID userId, WishlistItemAddRequest request);
    void removeItemFromWishlist(UUID userId, UUID wishlistId, UUID itemId);
    void deleteWishlist(UUID userId, UUID wishlistId);
}
