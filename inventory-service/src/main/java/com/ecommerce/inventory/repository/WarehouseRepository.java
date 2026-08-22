package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByCode(String code);
    Optional<Warehouse> findByIsDefaultTrue();
    List<Warehouse> findByIsActiveTrue();
    boolean existsByCode(String code);
}
