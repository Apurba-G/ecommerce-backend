package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.StockAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {
    List<StockAlert> findByIsResolvedFalse();
    Page<StockAlert> findByIsResolved(Boolean isResolved, Pageable pageable);
}
