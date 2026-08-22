package com.ecommerce.inventory.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.inventory.dto.WarehouseCreateRequest;
import com.ecommerce.inventory.dto.WarehouseDTO;
import com.ecommerce.inventory.service.WarehouseService;
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
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "APIs for physical fulfillment centers, regional hubs, and warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @Operation(summary = "Create a new warehouse", description = "Registers a new fulfillment center or warehouse location (Admin)")
    public ResponseEntity<ApiResponse<WarehouseDTO>> createWarehouse(@Valid @RequestBody WarehouseCreateRequest request) {
        WarehouseDTO created = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Warehouse created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all active warehouses", description = "Retrieves a list of all active fulfillment warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getAllActiveWarehouses() {
        List<WarehouseDTO> list = warehouseService.getAllActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success(list, "Active warehouses retrieved successfully"));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default warehouse", description = "Retrieves the primary default warehouse for dispatch")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getDefaultWarehouse() {
        WarehouseDTO warehouse = warehouseService.getDefaultWarehouse();
        return ResponseEntity.ok(ApiResponse.success(warehouse, "Default warehouse retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID", description = "Retrieves details of a specific warehouse by UUID")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getWarehouseById(
            @Parameter(description = "Warehouse UUID", required = true) @PathVariable("id") UUID id
    ) {
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(ApiResponse.success(warehouse, "Warehouse retrieved successfully"));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get warehouse by Code", description = "Retrieves details of a warehouse by its unique code (e.g. WH-CENTRAL-01)")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getWarehouseByCode(
            @Parameter(description = "Warehouse Code", required = true) @PathVariable("code") String code
    ) {
        WarehouseDTO warehouse = warehouseService.getWarehouseByCode(code);
        return ResponseEntity.ok(ApiResponse.success(warehouse, "Warehouse retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update warehouse details", description = "Updates address, manager, and operational status of a warehouse (Admin)")
    public ResponseEntity<ApiResponse<WarehouseDTO>> updateWarehouse(
            @Parameter(description = "Warehouse UUID", required = true) @PathVariable("id") UUID id,
            @Valid @RequestBody WarehouseCreateRequest request
    ) {
        WarehouseDTO updated = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Warehouse updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate warehouse", description = "Soft deletes/deactivates a warehouse location (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(
            @Parameter(description = "Warehouse UUID", required = true) @PathVariable("id") UUID id
    ) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Warehouse deactivated successfully"));
    }
}
