package com.ecommerce.payment.provider;

import com.ecommerce.payment.dto.InitiatePaymentRequest;
import com.ecommerce.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class MockPaymentProvider implements PaymentProviderStrategy {

    @Override
    public String getProviderName() {
        return "MOCK_PAYMENT_GATEWAY";
    }

    @Override
    public PaymentResult processPayment(Payment payment, InitiatePaymentRequest request) {
        log.info("Processing mock payment for orderId: {}, amount: {}", request.getOrderId(), request.getAmount());

        String gatewayOrdId = "MOCK-ORD-" + UUID.randomUUID().toString().substring(0, 8);
        String gatewayPayId = "MOCK-PAY-" + UUID.randomUUID().toString().substring(0, 8);

        return new PaymentResult(true, gatewayOrdId, gatewayPayId, null);
    }

    @Override
    public RefundResult processRefund(Payment payment, BigDecimal refundAmount, String reason) {
        log.info("Processing mock refund for paymentId: {}, amount: {}", payment.getId(), refundAmount);
        String gatewayRefId = "MOCK-REFUND-" + UUID.randomUUID().toString().substring(0, 8);
        return new RefundResult(true, gatewayRefId, null);
    }
}
