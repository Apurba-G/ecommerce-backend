package com.ecommerce.seller.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.seller.dto.CreateSellerRequest;
import com.ecommerce.seller.dto.SellerProfileDTO;
import com.ecommerce.seller.enums.SellerStatus;
import com.ecommerce.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller Controller", description = "Endpoints for Seller Profile Onboarding, Verification, Payouts, and Store Management")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new seller profile", description = "Initiates seller onboarding and KYC verification workflow")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> registerSeller(@Valid @RequestBody CreateSellerRequest request) {
        SellerProfileDTO seller = sellerService.registerSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(seller, "Seller registered successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get seller by ID", description = "Retrieves seller store details and bank account verification status")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> getSellerById(@PathVariable("id") UUID id) {
        SellerProfileDTO seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(ApiResponse.success(seller, "Seller profile retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get seller by User ID", description = "Retrieves seller profile associated with a user account")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> getSellerByUserId(@PathVariable("userId") UUID userId) {
        SellerProfileDTO seller = sellerService.getSellerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(seller, "Seller profile retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get sellers by status", description = "Retrieves paged list of sellers filtered by verification status e.g. PENDING, ACTIVE")
    public ResponseEntity<ApiResponse<PagedResponse<SellerProfileDTO>>> getSellersByStatus(
            @RequestParam(value = "status", defaultValue = "ACTIVE") SellerStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<SellerProfileDTO> sellers = sellerService.getSellersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(sellers, "Sellers retrieved successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update seller status (Admin / Moderation)", description = "Approves, suspends, or rejects seller store onboarding")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> updateSellerStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") SellerStatus status,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        SellerProfileDTO seller = sellerService.updateSellerStatus(id, status, reason);
        return ResponseEntity.ok(ApiResponse.success(seller, "Seller status updated successfully"));
    }
}
