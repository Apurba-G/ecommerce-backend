package com.ecommerce.shipping.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.shipping.dto.CreateShipmentRequest;
import com.ecommerce.shipping.dto.ShipmentDTO;
import com.ecommerce.shipping.dto.ShipmentTrackingDTO;
import com.ecommerce.shipping.dto.UpdateTrackingRequest;
import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.entity.ShipmentTracking;
import com.ecommerce.shipping.enums.ShipmentStatus;
import com.ecommerce.shipping.repository.ShipmentRepository;
import com.ecommerce.shipping.repository.ShipmentTrackingRepository;
import com.ecommerce.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository trackingRepository;

    @Override
    @Transactional
    public ShipmentDTO createShipment(CreateShipmentRequest request) {
        log.info("Creating shipment for orderId: {}, carrier: {}", request.getOrderId(), request.getCarrier());

        String trackingNumber = "TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String trackingUrl = "https://track.ecommerce.com/" + trackingNumber;

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .trackingNumber(trackingNumber)
                .carrier(request.getCarrier())
                .carrierTrackingUrl(trackingUrl)
                .shipmentStatus(ShipmentStatus.LABEL_CREATED)
                .shippingMethod(request.getShippingMethod())
                .shippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.valueOf(5.00))
                .estimatedWeight(request.getEstimatedWeight() != null ? request.getEstimatedWeight() : BigDecimal.valueOf(1.000))
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pinCode(request.getPinCode())
                .shippedAt(LocalDateTime.now())
                .estimatedDelivery(LocalDateTime.now().plusDays(3))
                .build();

        ShipmentTracking initialTracking = ShipmentTracking.builder()
                .status("LABEL_CREATED")
                .location(request.getCity())
                .description("Shipping label generated and ready for carrier pickup")
                .activityCode("LABEL_CREATED")
                .eventTime(LocalDateTime.now())
                .build();
        shipment.addTrackingEvent(initialTracking);

        Shipment saved = shipmentRepository.save(shipment);
        log.info("Shipment created with tracking number: {}", saved.getTrackingNumber());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDTO getShipmentById(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId.toString()));
        return mapToDTO(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDTO getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));
        return mapToDTO(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDTO getShipmentByOrderId(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "orderId", orderId.toString()));
        return mapToDTO(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ShipmentDTO> getShipmentsByUserId(UUID userId, Pageable pageable) {
        Page<Shipment> page = shipmentRepository.findByUserId(userId, pageable);
        List<ShipmentDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<ShipmentDTO>builder()
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
    public ShipmentDTO addTrackingEvent(String trackingNumber, UpdateTrackingRequest request) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));

        ShipmentTracking tracking = ShipmentTracking.builder()
                .status(request.getStatus())
                .location(request.getLocation())
                .description(request.getDescription())
                .activityCode(request.getActivityCode())
                .eventTime(LocalDateTime.now())
                .build();
        shipment.addTrackingEvent(tracking);

        try {
            ShipmentStatus newStatus = ShipmentStatus.valueOf(request.getStatus().toUpperCase());
            shipment.setShipmentStatus(newStatus);
            if (newStatus == ShipmentStatus.DELIVERED) {
                shipment.setDeliveredAt(LocalDateTime.now());
            }
        } catch (Exception ignored) {}

        Shipment updated = shipmentRepository.save(shipment);
        log.info("Added tracking event '{}' to shipment: {}", request.getStatus(), trackingNumber);
        return mapToDTO(updated);
    }

    private ShipmentDTO mapToDTO(Shipment shipment) {
        List<ShipmentTrackingDTO> trackingDTOs = shipment.getTrackingEvents() != null
                ? shipment.getTrackingEvents().stream().map(this::mapTrackingToDTO).collect(Collectors.toList())
                : List.of();

        return ShipmentDTO.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .userId(shipment.getUserId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .carrierTrackingUrl(shipment.getCarrierTrackingUrl())
                .shipmentStatus(shipment.getShipmentStatus())
                .shippingMethod(shipment.getShippingMethod())
                .shippingCost(shipment.getShippingCost())
                .estimatedWeight(shipment.getEstimatedWeight())
                .recipientName(shipment.getRecipientName())
                .recipientPhone(shipment.getRecipientPhone())
                .streetAddress(shipment.getStreetAddress())
                .city(shipment.getCity())
                .state(shipment.getState())
                .country(shipment.getCountry())
                .pinCode(shipment.getPinCode())
                .shippedAt(shipment.getShippedAt())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .trackingEvents(trackingDTOs)
                .build();
    }

    private ShipmentTrackingDTO mapTrackingToDTO(ShipmentTracking tracking) {
        return ShipmentTrackingDTO.builder()
                .id(tracking.getId())
                .status(tracking.getStatus())
                .location(tracking.getLocation())
                .description(tracking.getDescription())
                .activityCode(tracking.getActivityCode())
                .eventTime(tracking.getEventTime())
                .createdAt(tracking.getCreatedAt())
                .build();
    }
}
