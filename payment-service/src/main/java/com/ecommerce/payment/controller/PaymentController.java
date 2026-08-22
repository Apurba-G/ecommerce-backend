package com.ecommerce.payment.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.payment.dto.InitiatePaymentRequest;
import com.ecommerce.payment.dto.PaymentDTO;
import com.ecommerce.payment.dto.RefundDTO;
import com.ecommerce.payment.dto.RefundRequest;
import com.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Endpoints for Payment Authorization, Idempotency Processing, and Refunds")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Initiate and process a payment", description = "Processes payment using configured provider with strict idempotency key protection")
    public ResponseEntity<ApiResponse<PaymentDTO>> processPayment(@Valid @RequestBody InitiatePaymentRequest request) {
        PaymentDTO payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment processed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment authorization and transaction record")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable("id") UUID id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by Order ID", description = "Retrieves payment record for a given order")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByOrderId(@PathVariable("orderId") UUID orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }

    @PostMapping("/refund")
    @Operation(summary = "Process a refund", description = "Initiates a refund for a previously successful payment")
    public ResponseEntity<ApiResponse<RefundDTO>> processRefund(@Valid @RequestBody RefundRequest request) {
        RefundDTO refund = paymentService.processRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(refund, "Refund processed successfully"));
    }
}
