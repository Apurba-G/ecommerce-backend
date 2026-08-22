package com.ecommerce.payment.provider;

import com.ecommerce.payment.dto.InitiatePaymentRequest;
import com.ecommerce.payment.entity.Payment;

public interface PaymentProviderStrategy {

    String getProviderName();

    PaymentResult processPayment(Payment payment, InitiatePaymentRequest request);

    RefundResult processRefund(Payment payment, java.math.BigDecimal refundAmount, String reason);

    record PaymentResult(boolean success, String gatewayOrderId, String gatewayPaymentId, String failureReason) {}
    record RefundResult(boolean success, String gatewayRefundId, String failureReason) {}
}
