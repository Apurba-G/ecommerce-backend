package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartDTO;
import com.ecommerce.cart.dto.CartItemAddRequest;
import com.ecommerce.cart.dto.CartItemUpdateRequest;

import java.util.UUID;

public interface CartService {
    CartDTO getCart(UUID userId, String sessionId);
    CartDTO addItemToCart(UUID userId, String sessionId, CartItemAddRequest request);
    CartDTO updateItemQuantity(UUID userId, String sessionId, UUID itemId, CartItemUpdateRequest request);
    CartDTO removeItem(UUID userId, String sessionId, UUID itemId);
    CartDTO clearCart(UUID userId, String sessionId);
    CartDTO mergeGuestCart(UUID userId, String guestSessionId);
    CartDTO applyCoupon(UUID userId, String sessionId, String couponCode);
}
