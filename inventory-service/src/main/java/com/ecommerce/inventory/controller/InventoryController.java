package com.ecommerce.inventory.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory & Stock Control", description = "APIs for SKU stock levels, reservations, adjustments, and ledger tracking")
public class InventoryController {

    private final InventoryService inventoryService;

    private UUID parseUserId(String header) {
        if (header == null || header.isBlank()) return null;
        return UUID.fromString(header.replace("\"", "").trim());
    }

    @PostMapping("/adjust")
    @Operation(summary = "Adjust product inventory stock", description = "Increases or decreases inventory stock for a product/variant in a specific warehouse (Seller / Admin)")
    public ResponseEntity<ApiResponse<InventoryDTO>> adjustStock(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @Valid @RequestBody InventoryStockAdjustmentRequest request
    ) {
        UUID sellerId = parseUserId(userIdHeader);
        InventoryDTO updated = inventoryService.adjustStock(sellerId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Inventory stock adjusted successfully"));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for order checkout", description = "Temporarily locks available stock during order checkout")
    public ResponseEntity<ApiResponse<InventoryDTO>> reserveStock(@Valid @RequestBody InventoryReserveRequest request) {
        InventoryDTO reserved = inventoryService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success(reserved, "Stock reserved successfully"));
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved stock", description = "Releases previously locked stock back to available pool on cart/order cancellation")
    public ResponseEntity<ApiResponse<InventoryDTO>> releaseStock(@Valid @RequestBody InventoryReserveRequest request) {
        InventoryDTO released = inventoryService.releaseStock(request);
        return ResponseEntity.ok(ApiResponse.success(released, "Stock reservation released successfully"));
    }

    @PostMapping("/deduct")
    @Operation(summary = "Deduct reserved stock upon payment", description = "Permanently deducts reserved stock and increments sold count upon payment confirmation")
    public ResponseEntity<ApiResponse<InventoryDTO>> deductStock(@Valid @RequestBody InventoryReserveRequest request) {
        InventoryDTO deducted = inventoryService.deductReservedStock(request);
        return ResponseEntity.ok(ApiResponse.success(deducted, "Stock deducted successfully"));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock across all warehouses for a product", description = "Retrieves stock distribution across warehouses for a product or variant")
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getProductStock(
            @Parameter(description = "Product UUID", required = true) @PathVariable("productId") UUID productId,
            @Parameter(description = "Optional Variant UUID") @RequestParam(value = "variantId", required = false) UUID variantId
    ) {
        List<InventoryDTO> list = inventoryService.getStockByProduct(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(list, "Stock details retrieved successfully"));
    }

    @GetMapping("/product/{productId}/available")
    @Operation(summary = "Get total available stock count", description = "Returns aggregated available stock count across all warehouses (Cached)")
    public ResponseEntity<ApiResponse<Integer>> getTotalAvailableStock(
            @Parameter(description = "Product UUID", required = true) @PathVariable("productId") UUID productId,
            @Parameter(description = "Optional Variant UUID") @RequestParam(value = "variantId", required = false) UUID variantId
    ) {
        int available = inventoryService.getTotalAvailableStock(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(available, "Total available stock retrieved successfully"));
    }

    @GetMapping("/{inventoryId}/transactions")
    @Operation(summary = "Get audit ledger transactions for inventory", description = "Retrieves double-entry stock audit transaction history")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryTransactionDTO>>> getTransactions(
            @Parameter(description = "Inventory UUID", required = true) @PathVariable("inventoryId") UUID inventoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<InventoryTransactionDTO> txs = inventoryService.getInventoryTransactions(inventoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(txs, "Audit transactions retrieved successfully"));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get unresolved low-stock alerts", description = "Retrieves list of active low-stock and out-of-stock alerts for seller/admin review")
    public ResponseEntity<ApiResponse<List<StockAlertDTO>>> getUnresolvedAlerts() {
        List<StockAlertDTO> alerts = inventoryService.getUnresolvedStockAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts, "Stock alerts retrieved successfully"));
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolve stock alert", description = "Marks a low-stock alert as resolved (Admin / Seller)")
    public ResponseEntity<ApiResponse<StockAlertDTO>> resolveAlert(
            @Parameter(description = "Stock Alert UUID", required = true) @PathVariable("alertId") UUID alertId
    ) {
        StockAlertDTO resolved = inventoryService.resolveStockAlert(alertId);
        return ResponseEntity.ok(ApiResponse.success(resolved, "Stock alert resolved successfully"));
    }
}
