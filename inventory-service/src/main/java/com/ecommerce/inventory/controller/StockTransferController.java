package com.ecommerce.inventory.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.inventory.dto.StockTransferCreateRequest;
import com.ecommerce.inventory.dto.StockTransferDTO;
import com.ecommerce.inventory.service.StockTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/inventory/transfers")
@RequiredArgsConstructor
@Tag(name = "Stock Transfers", description = "APIs for moving stock between physical warehouses")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    private UUID parseUserId(String header) {
        if (header == null || header.isBlank()) return null;
        return UUID.fromString(header.replace("\"", "").trim());
    }

    @PostMapping
    @Operation(summary = "Initiate stock transfer", description = "Moves stock from source warehouse to destination warehouse (Admin / Logistics)")
    public ResponseEntity<ApiResponse<StockTransferDTO>> initiateTransfer(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @Valid @RequestBody StockTransferCreateRequest request
    ) {
        UUID initiatorId = parseUserId(userIdHeader);
        StockTransferDTO transfer = stockTransferService.initiateTransfer(initiatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transfer, "Stock transfer initiated successfully"));
    }

    @PatchMapping("/{transferId}/complete")
    @Operation(summary = "Complete stock transfer", description = "Confirms delivery of stock at destination warehouse and updates inventory")
    public ResponseEntity<ApiResponse<StockTransferDTO>> completeTransfer(
            @Parameter(description = "Transfer UUID", required = true) @PathVariable("transferId") UUID transferId
    ) {
        StockTransferDTO completed = stockTransferService.completeTransfer(transferId);
        return ResponseEntity.ok(ApiResponse.success(completed, "Stock transfer completed successfully"));
    }

    @PatchMapping("/{transferId}/cancel")
    @Operation(summary = "Cancel stock transfer", description = "Cancels in-transit transfer and returns stock to source warehouse")
    public ResponseEntity<ApiResponse<StockTransferDTO>> cancelTransfer(
            @Parameter(description = "Transfer UUID", required = true) @PathVariable("transferId") UUID transferId
    ) {
        StockTransferDTO cancelled = stockTransferService.cancelTransfer(transferId);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Stock transfer cancelled successfully"));
    }

    @GetMapping
    @Operation(summary = "List stock transfers", description = "Retrieves paginated list of all inter-warehouse stock transfers")
    public ResponseEntity<ApiResponse<PagedResponse<StockTransferDTO>>> getTransfers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<StockTransferDTO> transfers = stockTransferService.getTransfers(pageable);
        return ResponseEntity.ok(ApiResponse.success(transfers, "Stock transfers retrieved successfully"));
    }
}
