package com.ecommerce.shipping.repository;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByOrderId(UUID orderId);

    Page<Shipment> findByUserId(UUID userId, Pageable pageable);

    Page<Shipment> findByShipmentStatus(ShipmentStatus status, Pageable pageable);
}
