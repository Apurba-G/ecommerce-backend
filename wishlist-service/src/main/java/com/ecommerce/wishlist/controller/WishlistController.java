package com.ecommerce.wishlist.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.wishlist.dto.WishlistCreateRequest;
import com.ecommerce.wishlist.dto.WishlistDTO;
import com.ecommerce.wishlist.dto.WishlistItemAddRequest;
import com.ecommerce.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist & Registry Management", description = "APIs for saving favorite products, managing multiple wishlists, and sharing gift registries")
public class WishlistController {

    private final WishlistService wishlistService;

    private UUID parseUserId(String header) {
        if (header == null || header.isBlank()) return null;
        return UUID.fromString(header.replace("\"", "").trim());
    }

    @PostMapping
    @Operation(summary = "Create a custom wishlist", description = "Creates a new custom wishlist (e.g. 'Birthday Gifts', 'Tech Gear')")
    public ResponseEntity<ApiResponse<WishlistDTO>> createWishlist(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody WishlistCreateRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        WishlistDTO created = wishlistService.createWishlist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Wishlist created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get user wishlists", description = "Retrieves all wishlists created by the authenticated customer")
    public ResponseEntity<ApiResponse<List<WishlistDTO>>> getUserWishlists(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        List<WishlistDTO> list = wishlistService.getUserWishlists(userId);
        return ResponseEntity.ok(ApiResponse.success(list, "Wishlists retrieved successfully"));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default primary wishlist", description = "Retrieves or auto-provisions the customer's primary 'My Favorites' wishlist")
    public ResponseEntity<ApiResponse<WishlistDTO>> getDefaultWishlist(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        WishlistDTO wishlist = wishlistService.getDefaultWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Default wishlist retrieved successfully"));
    }

    @GetMapping("/{wishlistId}")
    @Operation(summary = "Get wishlist by ID", description = "Retrieves specific wishlist and all its saved product items")
    public ResponseEntity<ApiResponse<WishlistDTO>> getWishlistById(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Parameter(description = "Wishlist UUID", required = true) @PathVariable("wishlistId") UUID wishlistId
    ) {
        UUID userId = parseUserId(userIdHeader);
        WishlistDTO wishlist = wishlistService.getWishlistById(userId, wishlistId);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist retrieved successfully"));
    }

    @GetMapping("/share/{shareToken}")
    @Operation(summary = "View public shared wishlist", description = "Public endpoint to view a shared gift registry via public share token")
    public ResponseEntity<ApiResponse<WishlistDTO>> getPublicWishlist(
            @Parameter(description = "Share token string", required = true) @PathVariable("shareToken") String shareToken
    ) {
        WishlistDTO wishlist = wishlistService.getPublicWishlistByShareToken(shareToken);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Public wishlist retrieved successfully"));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to default wishlist", description = "Quick-adds a product snapshot into the customer's default 'My Favorites' wishlist")
    public ResponseEntity<ApiResponse<WishlistDTO>> addItemToDefaultWishlist(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody WishlistItemAddRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        WishlistDTO updated = wishlistService.addItemToDefaultWishlist(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Item added to wishlist successfully"));
    }

    @PostMapping("/{wishlistId}/items")
    @Operation(summary = "Add item to specific wishlist", description = "Adds a product snapshot into a specified custom wishlist")
    public ResponseEntity<ApiResponse<WishlistDTO>> addItemToWishlist(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Parameter(description = "Wishlist UUID", required = true) @PathVariable("wishlistId") UUID wishlistId,
            @Valid @RequestBody WishlistItemAddRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        WishlistDTO updated = wishlistService.addItemToWishlist(userId, wishlistId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Item added to wishlist successfully"));
    }

    @DeleteMapping("/{wishlistId}/items/{itemId}")
    @Operation(summary = "Remove item from wishlist", description = "Removes a product entry from the wishlist")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Parameter(description = "Wishlist UUID", required = true) @PathVariable("wishlistId") UUID wishlistId,
            @Parameter(description = "Wishlist Item UUID", required = true) @PathVariable("itemId") UUID itemId
    ) {
        UUID userId = parseUserId(userIdHeader);
        wishlistService.removeItemFromWishlist(userId, wishlistId, itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from wishlist successfully"));
    }

    @DeleteMapping("/{wishlistId}")
    @Operation(summary = "Delete wishlist", description = "Permanently deletes a custom wishlist")
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Parameter(description = "Wishlist UUID", required = true) @PathVariable("wishlistId") UUID wishlistId
    ) {
        UUID userId = parseUserId(userIdHeader);
        wishlistService.deleteWishlist(userId, wishlistId);
        return ResponseEntity.ok(ApiResponse.success(null, "Wishlist deleted successfully"));
    }
}
