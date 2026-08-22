package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderAddress;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatusHistory;
import com.ecommerce.order.enums.AddressType;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.enums.PaymentStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.saga.OrderSagaOrchestrator;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating new order for userId: {}", request.getUserId());

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(request.getUserId())
                .sellerId(request.getSellerId())
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .couponCode(request.getCouponCode())
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .shippingAmount(BigDecimal.valueOf(5.00))
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal calculatedSubtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal itemTotal = itemReq.getSellingPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            calculatedSubtotal = calculatedSubtotal.add(itemTotal);

            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .variantId(itemReq.getVariantId())
                    .productName(itemReq.getProductName())
                    .productImage(itemReq.getProductImage())
                    .productSku(itemReq.getProductSku())
                    .unitPrice(itemReq.getUnitPrice())
                    .sellingPrice(itemReq.getSellingPrice())
                    .quantity(itemReq.getQuantity())
                    .totalPrice(itemTotal)
                    .build();
            order.addItem(item);
        }

        order.setSubtotal(calculatedSubtotal);

        BigDecimal tax = calculatedSubtotal.multiply(BigDecimal.valueOf(0.05)); // 5% tax
        order.setTaxAmount(tax);

        BigDecimal total = calculatedSubtotal.add(tax).add(order.getShippingAmount()).subtract(order.getDiscountAmount());
        order.setTotalAmount(total);

        // Add Shipping Address
        if (request.getShippingAddress() != null) {
            OrderAddress shippingAddr = mapToAddressEntity(request.getShippingAddress(), AddressType.SHIPPING);
            order.addAddress(shippingAddr);
        }

        // Add Status History
        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING.name())
                .changedBy("SYSTEM")
                .notes("Order created and waiting for payment authorization")
                .build();
        order.addStatusHistory(initialHistory);

        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created order with ID: {} and Number: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        // Trigger Transactional Outbox Event for Saga Orchestration
        sagaOrchestrator.startOrderCheckoutSaga(savedOrder);

        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));
        return mapToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderDTO> getOrdersByUserId(UUID userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        List<OrderDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<OrderDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderDTO> getOrdersByUserIdAndStatus(UUID userId, OrderStatus status, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserIdAndOrderStatus(userId, status, pageable);
        List<OrderDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<OrderDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(request.getStatus());

        if (request.getStatus() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        } else if (request.getStatus() == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
            if (request.getCancellationReason() != null) {
                order.setCancellationReason(request.getCancellationReason());
            }
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .fromStatus(oldStatus != null ? oldStatus.name() : null)
                .toStatus(request.getStatus().name())
                .changedBy(request.getChangedBy() != null ? request.getChangedBy() : "ADMIN")
                .notes(request.getNotes())
                .build();
        order.addStatusHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated for orderId: {} from {} to {}", orderId, oldStatus, request.getStatus());
        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));

        if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        OrderStatusHistory history = OrderStatusHistory.builder()
                .fromStatus(order.getOrderStatus().name())
                .toStatus(OrderStatus.CANCELLED.name())
                .changedBy("CUSTOMER")
                .notes(reason)
                .build();
        order.addStatusHistory(history);

        Order saved = orderRepository.save(order);
        sagaOrchestrator.compensateOrderFailure(saved, reason);
        return mapToDTO(saved);
    }

    private String generateOrderNumber() {
        try {
            return jdbcTemplate.queryForObject("SELECT generate_order_number()", String.class);
        } catch (Exception e) {
            return "ORD-" + System.currentTimeMillis();
        }
    }

    private OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems() != null
                ? order.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList())
                : List.of();

        List<OrderAddressDTO> addressDTOs = order.getAddresses() != null
                ? order.getAddresses().stream().map(this::mapAddressToDTO).collect(Collectors.toList())
                : List.of();

        List<OrderStatusHistoryDTO> historyDTOs = order.getStatusHistory() != null
                ? order.getStatusHistory().stream().map(this::mapHistoryToDTO).collect(Collectors.toList())
                : List.of();

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .sellerId(order.getSellerId())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .totalAmount(order.getTotalAmount())
                .couponId(order.getCouponId())
                .couponCode(order.getCouponCode())
                .notes(order.getNotes())
                .cancellationReason(order.getCancellationReason())
                .returnReason(order.getReturnReason())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .returnedAt(order.getReturnedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .addresses(addressDTOs)
                .statusHistory(historyDTOs)
                .build();
    }

    private OrderItemDTO mapItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .productSku(item.getProductSku())
                .unitPrice(item.getUnitPrice())
                .sellingPrice(item.getSellingPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .itemStatus(item.getItemStatus())
                .returnReason(item.getReturnReason())
                .returnedAt(item.getReturnedAt())
                .build();
    }

    private OrderAddressDTO mapAddressToDTO(OrderAddress address) {
        return OrderAddressDTO.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    private OrderStatusHistoryDTO mapHistoryToDTO(OrderStatusHistory history) {
        return OrderStatusHistoryDTO.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedBy(history.getChangedBy())
                .notes(history.getNotes())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private OrderAddress mapToAddressEntity(OrderAddressDTO dto, AddressType type) {
        return OrderAddress.builder()
                .addressType(type)
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry() != null ? dto.getCountry() : "India")
                .postalCode(dto.getPostalCode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }
}
