package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.InitiatePaymentRequest;
import com.ecommerce.payment.dto.PaymentDTO;
import com.ecommerce.payment.dto.RefundDTO;
import com.ecommerce.payment.dto.RefundRequest;

import java.util.UUID;

public interface PaymentService {

    PaymentDTO processPayment(InitiatePaymentRequest request);

    PaymentDTO getPaymentById(UUID paymentId);

    PaymentDTO getPaymentByOrderId(UUID orderId);

    RefundDTO processRefund(RefundRequest request);
}
