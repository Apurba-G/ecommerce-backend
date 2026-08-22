package com.ecommerce.shipping.entity;

import com.ecommerce.shipping.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 100)
    private String trackingNumber;

    @Column(name = "carrier", nullable = false, length = 100)
    private String carrier;

    @Column(name = "carrier_tracking_url", columnDefinition = "TEXT")
    private String carrierTrackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false, length = 40)
    @Builder.Default
    private ShipmentStatus shipmentStatus = ShipmentStatus.PENDING;

    @Column(name = "shipping_method", nullable = false, length = 50)
    private String shippingMethod;

    @Column(name = "shipping_cost", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "estimated_weight", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal estimatedWeight = BigDecimal.ZERO;

    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 30)
    private String recipientPhone;

    @Column(name = "street_address", nullable = false, length = 500)
    private String streetAddress;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "pin_code", length = 20)
    private String pinCode;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShipmentTracking> trackingEvents = new ArrayList<>();

    public void addTrackingEvent(ShipmentTracking tracking) {
        trackingEvents.add(tracking);
        tracking.setShipment(this);
    }
}
