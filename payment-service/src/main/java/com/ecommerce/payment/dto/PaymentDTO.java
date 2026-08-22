package com.ecommerce.payment.dto;

import com.ecommerce.payment.enums.PaymentMethodType;
import com.ecommerce.payment.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private PaymentMethodType paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String currency;
    private String gateway;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String idempotencyKey;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
