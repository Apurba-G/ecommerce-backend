package com.ecommerce.shipping.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.shipping.dto.CreateShipmentRequest;
import com.ecommerce.shipping.dto.ShipmentDTO;
import com.ecommerce.shipping.dto.UpdateTrackingRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShippingService {

    ShipmentDTO createShipment(CreateShipmentRequest request);

    ShipmentDTO getShipmentById(UUID shipmentId);

    ShipmentDTO getShipmentByTrackingNumber(String trackingNumber);

    ShipmentDTO getShipmentByOrderId(UUID orderId);

    PagedResponse<ShipmentDTO> getShipmentsByUserId(UUID userId, Pageable pageable);

    ShipmentDTO addTrackingEvent(String trackingNumber, UpdateTrackingRequest request);
}
