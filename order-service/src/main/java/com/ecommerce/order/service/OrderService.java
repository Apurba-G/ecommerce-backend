package com.ecommerce.order.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderStatusUpdateRequest;
import com.ecommerce.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderDTO createOrder(CreateOrderRequest request);

    OrderDTO getOrderById(UUID orderId);

    OrderDTO getOrderByOrderNumber(String orderNumber);

    PagedResponse<OrderDTO> getOrdersByUserId(UUID userId, Pageable pageable);

    PagedResponse<OrderDTO> getOrdersByUserIdAndStatus(UUID userId, OrderStatus status, Pageable pageable);

    OrderDTO updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);

    OrderDTO cancelOrder(UUID orderId, String reason);
}
