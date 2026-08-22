package com.ecommerce.payment.dto;

import com.ecommerce.payment.enums.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundDTO {
    private UUID id;
    private UUID paymentId;
    private UUID orderId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String gatewayRefundId;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
