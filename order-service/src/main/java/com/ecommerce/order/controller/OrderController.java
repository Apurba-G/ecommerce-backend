package com.ecommerce.order.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderStatusUpdateRequest;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.service.OrderService;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Endpoints for Order Management, Checkout Saga, and Status Workflows")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Placing an order triggers the Order Checkout Saga and Outbox Events")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order placed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves complete order details including items, addresses, and status history")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable("id") UUID id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by Order Number", description = "Retrieves order using readable order number e.g. ORD-20260819-001001")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByOrderNumber(@PathVariable("orderNumber") String orderNumber) {
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user order history", description = "Retrieves paginated order history for a customer")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getOrdersByUserId(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<OrderDTO> orders = (status != null)
                ? orderService.getOrdersByUserIdAndStatus(userId, status, pageable)
                : orderService.getOrdersByUserId(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(orders, "User orders retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates order status (e.g. PACKED, SHIPPED, DELIVERED)")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order status updated successfully"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Cancels an active order and triggers Saga compensating workflow")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable("id") UUID id,
            @Parameter(description = "Cancellation reason") @RequestParam(value = "reason", defaultValue = "Customer requested cancellation") String reason
    ) {
        OrderDTO cancelledOrder = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success(cancelledOrder, "Order cancelled successfully"));
    }
}
