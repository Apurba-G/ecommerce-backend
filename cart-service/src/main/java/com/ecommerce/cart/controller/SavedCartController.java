package com.ecommerce.cart.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.cart.dto.SavedCartCreateRequest;
import com.ecommerce.cart.dto.SavedCartDTO;
import com.ecommerce.cart.service.SavedCartService;
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
@RequestMapping("/api/v1/saved-carts")
@RequiredArgsConstructor
@Tag(name = "Saved Carts", description = "APIs for saving shopping baskets for future bulk purchasing")
public class SavedCartController {

    private final SavedCartService savedCartService;

    private UUID parseUserId(String header) {
        if (header == null || header.isBlank()) return null;
        return UUID.fromString(header.replace("\"", "").trim());
    }

    @PostMapping
    @Operation(summary = "Save current active cart", description = "Snapshots the current active cart as a saved cart for later retrieval")
    public ResponseEntity<ApiResponse<SavedCartDTO>> saveCart(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody SavedCartCreateRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        SavedCartDTO saved = savedCartService.saveCurrentCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Cart saved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get user saved carts", description = "Retrieves all saved shopping carts for the authenticated customer")
    public ResponseEntity<ApiResponse<List<SavedCartDTO>>> getSavedCarts(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        List<SavedCartDTO> list = savedCartService.getSavedCarts(userId);
        return ResponseEntity.ok(ApiResponse.success(list, "Saved carts retrieved successfully"));
    }

    @DeleteMapping("/{savedCartId}")
    @Operation(summary = "Delete saved cart", description = "Deletes a previously saved shopping cart")
    public ResponseEntity<ApiResponse<Void>> deleteSavedCart(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Parameter(description = "Saved Cart UUID", required = true) @PathVariable("savedCartId") UUID savedCartId
    ) {
        UUID userId = parseUserId(userIdHeader);
        savedCartService.deleteSavedCart(userId, savedCartId);
        return ResponseEntity.ok(ApiResponse.success(null, "Saved cart deleted successfully"));
    }
}
