package com.ecommerce.order.dto;

import com.ecommerce.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private String changedBy;

    private String notes;

    private String cancellationReason;
}
