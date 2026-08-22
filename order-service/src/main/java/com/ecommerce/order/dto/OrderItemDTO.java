package com.ecommerce.order.dto;

import com.ecommerce.order.enums.OrderItemStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String productImage;
    private String productSku;
    private BigDecimal unitPrice;
    private BigDecimal sellingPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private OrderItemStatus itemStatus;
    private String returnReason;
    private LocalDateTime returnedAt;
}
