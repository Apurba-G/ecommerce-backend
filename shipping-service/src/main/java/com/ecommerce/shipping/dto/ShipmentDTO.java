package com.ecommerce.shipping.dto;

import com.ecommerce.shipping.enums.ShipmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDTO {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private String trackingNumber;
    private String carrier;
    private String carrierTrackingUrl;
    private ShipmentStatus shipmentStatus;
    private String shippingMethod;
    private BigDecimal shippingCost;
    private BigDecimal estimatedWeight;
    private String recipientName;
    private String recipientPhone;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String pinCode;
    private LocalDateTime shippedAt;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShipmentTrackingDTO> trackingEvents;
}
