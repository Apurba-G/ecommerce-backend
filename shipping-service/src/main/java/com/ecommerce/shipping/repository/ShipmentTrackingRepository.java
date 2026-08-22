package com.ecommerce.shipping.repository;

import com.ecommerce.shipping.entity.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, UUID> {

    List<ShipmentTracking> findByShipmentIdOrderByEventTimeAsc(UUID shipmentId);
}
