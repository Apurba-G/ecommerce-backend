package com.ecommerce.cart.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.cart.dto.CartDTO;
import com.ecommerce.cart.dto.CartItemAddRequest;
import com.ecommerce.cart.dto.CartItemUpdateRequest;
import com.ecommerce.cart.dto.CartMergeRequest;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "APIs for active shopping carts, guest sessions, real-time total recalculation, and cart merging")
public class CartController {

    private final CartService cartService;

    private UUID parseUserId(String header) {
        if (header == null || header.isBlank()) return null;
        return UUID.fromString(header.replace("\"", "").trim());
    }

    @GetMapping
    @Operation(summary = "Get active shopping cart", description = "Retrieves active shopping cart for authenticated user or anonymous guest session")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO cart = cartService.getCart(userId, sessionIdHeader);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product variant to cart and automatically updates subtotal, discount, and taxes")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO updated = cartService.addItemToCart(userId, sessionIdHeader, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Item added to cart successfully"));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity", description = "Updates quantity for a specific line item in the cart")
    public ResponseEntity<ApiResponse<CartDTO>> updateQuantity(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader,
            @Parameter(description = "Cart Item UUID", required = true) @PathVariable("itemId") UUID itemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO updated = cartService.updateItemQuantity(userId, sessionIdHeader, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes a specific line item from the shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader,
            @Parameter(description = "Cart Item UUID", required = true) @PathVariable("itemId") UUID itemId
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO updated = cartService.removeItem(userId, sessionIdHeader, itemId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear shopping cart", description = "Empties all items from the active cart")
    public ResponseEntity<ApiResponse<CartDTO>> clearCart(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO cleared = cartService.clearCart(userId, sessionIdHeader);
        return ResponseEntity.ok(ApiResponse.success(cleared, "Cart cleared successfully"));
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge guest cart into authenticated cart", description = "Merges anonymous guest session items into the authenticated customer's cart upon login")
    public ResponseEntity<ApiResponse<CartDTO>> mergeGuestCart(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody CartMergeRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO merged = cartService.mergeGuestCart(userId, request.getGuestSessionId());
        return ResponseEntity.ok(ApiResponse.success(merged, "Guest cart merged successfully"));
    }

    @PostMapping("/coupon")
    @Operation(summary = "Apply promo coupon to cart", description = "Applies discount voucher to shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> applyCoupon(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader,
            @RequestParam("code") String couponCode
    ) {
        UUID userId = parseUserId(userIdHeader);
        CartDTO updated = cartService.applyCoupon(userId, sessionIdHeader, couponCode);
        return ResponseEntity.ok(ApiResponse.success(updated, "Coupon applied successfully"));
    }
}
