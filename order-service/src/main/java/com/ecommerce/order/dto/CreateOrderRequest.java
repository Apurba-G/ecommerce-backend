package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private UUID sellerId;

    @NotEmpty(message = "Order items list cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address is required")
    private OrderAddressDTO shippingAddress;

    private OrderAddressDTO billingAddress;

    private String couponCode;

    private String notes;
}
