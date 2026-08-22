package com.ecommerce.payment.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.payment.dto.InitiatePaymentRequest;
import com.ecommerce.payment.dto.PaymentDTO;
import com.ecommerce.payment.dto.RefundDTO;
import com.ecommerce.payment.dto.RefundRequest;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentTransaction;
import com.ecommerce.payment.entity.Refund;
import com.ecommerce.payment.enums.PaymentStatus;
import com.ecommerce.payment.enums.RefundStatus;
import com.ecommerce.payment.enums.TransactionStatus;
import com.ecommerce.payment.enums.TransactionType;
import com.ecommerce.payment.provider.MockPaymentProvider;
import com.ecommerce.payment.provider.PaymentProviderStrategy;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.RefundRepository;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final MockPaymentProvider mockPaymentProvider;

    @Override
    @Transactional
    public PaymentDTO processPayment(InitiatePaymentRequest request) {
        log.info("Processing payment for orderId: {}, idempotencyKey: {}", request.getOrderId(), request.getIdempotencyKey());

        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingPayment.isPresent()) {
            log.info("Idempotency hit! Returning existing payment ID: {}", existingPayment.get().getId());
            return mapToPaymentDTO(existingPayment.get());
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PROCESSING)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .gateway(request.getGateway() != null ? request.getGateway() : mockPaymentProvider.getProviderName())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        PaymentProviderStrategy.PaymentResult result = mockPaymentProvider.processPayment(payment, request);

        if (result.success()) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setGatewayOrderId(result.gatewayOrderId());
            payment.setGatewayPaymentId(result.gatewayPaymentId());
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .payment(payment)
                .transactionType(TransactionType.CHARGE)
                .amount(request.getAmount())
                .status(result.success() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .gatewayReference(result.gatewayPaymentId())
                .failureReason(result.failureReason())
                .build();
        payment.getTransactions().add(tx);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with status: {} for paymentId: {}", savedPayment.getPaymentStatus(), savedPayment.getId());

        return mapToPaymentDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId.toString()));
        return mapToPaymentDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId.toString()));
        return mapToPaymentDTO(payment);
    }

    @Override
    @Transactional
    public RefundDTO processRefund(RefundRequest request) {
        log.info("Processing refund for paymentId: {}, amount: {}", request.getPaymentId(), request.getAmount());

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId().toString()));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot refund payment that is not SUCCESS. Current status: " + payment.getPaymentStatus());
        }

        PaymentProviderStrategy.RefundResult result = mockPaymentProvider.processRefund(payment, request.getAmount(), request.getReason());

        Refund refund = Refund.builder()
                .payment(payment)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(result.success() ? RefundStatus.COMPLETED : RefundStatus.FAILED)
                .gatewayRefundId(result.gatewayRefundId())
                .processedAt(result.success() ? LocalDateTime.now() : null)
                .build();

        Refund savedRefund = refundRepository.save(refund);

        if (result.success()) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        return mapToRefundDTO(savedRefund);
    }

    private PaymentDTO mapToPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .gateway(payment.getGateway())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .idempotencyKey(payment.getIdempotencyKey())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private RefundDTO mapToRefundDTO(Refund refund) {
        return RefundDTO.builder()
                .id(refund.getId())
                .paymentId(refund.getPayment().getId())
                .orderId(refund.getOrderId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .gatewayRefundId(refund.getGatewayRefundId())
                .processedAt(refund.getProcessedAt())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
