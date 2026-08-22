package com.ecommerce.shipping.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.shipping.dto.CreateShipmentRequest;
import com.ecommerce.shipping.dto.ShipmentDTO;
import com.ecommerce.shipping.dto.UpdateTrackingRequest;
import com.ecommerce.shipping.service.ShippingService;
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
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping Controller", description = "Endpoints for Logistics, Tracking Events, and Order Shipments")
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/shipments")
    @Operation(summary = "Create a new shipment", description = "Generates a tracking number and initializes logistics carrier dispatch")
    public ResponseEntity<ApiResponse<ShipmentDTO>> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentDTO shipment = shippingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(shipment, "Shipment created successfully"));
    }

    @GetMapping("/shipments/{id}")
    @Operation(summary = "Get shipment by ID", description = "Retrieves shipment details and full tracking history")
    public ResponseEntity<ApiResponse<ShipmentDTO>> getShipmentById(@PathVariable("id") UUID id) {
        ShipmentDTO shipment = shippingService.getShipmentById(id);
        return ResponseEntity.ok(ApiResponse.success(shipment, "Shipment retrieved successfully"));
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number", description = "Public tracking endpoint for real-time order tracking e.g. TRK-EXPRESS-0001001")
    public ResponseEntity<ApiResponse<ShipmentDTO>> getShipmentByTrackingNumber(@PathVariable("trackingNumber") String trackingNumber) {
        ShipmentDTO shipment = shippingService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(shipment, "Tracking events retrieved successfully"));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipment by Order ID", description = "Retrieves logistics record for a given order")
    public ResponseEntity<ApiResponse<ShipmentDTO>> getShipmentByOrderId(@PathVariable("orderId") UUID orderId) {
        ShipmentDTO shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(shipment, "Shipment retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user shipments", description = "Retrieves customer shipment history")
    public ResponseEntity<ApiResponse<PagedResponse<ShipmentDTO>>> getShipmentsByUserId(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ShipmentDTO> shipments = shippingService.getShipmentsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(shipments, "User shipments retrieved successfully"));
    }

    @PostMapping("/track/{trackingNumber}/events")
    @Operation(summary = "Add carrier tracking event", description = "Webhook or admin endpoint to append carrier status updates (e.g. IN_TRANSIT, DELIVERED)")
    public ResponseEntity<ApiResponse<ShipmentDTO>> addTrackingEvent(
            @PathVariable("trackingNumber") String trackingNumber,
            @Valid @RequestBody UpdateTrackingRequest request
    ) {
        ShipmentDTO shipment = shippingService.addTrackingEvent(trackingNumber, request);
        return ResponseEntity.ok(ApiResponse.success(shipment, "Tracking event added successfully"));
    }
}
