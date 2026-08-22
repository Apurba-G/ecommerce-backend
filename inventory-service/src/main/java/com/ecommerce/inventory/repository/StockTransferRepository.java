package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {
    Page<StockTransfer> findByStatus(String status, Pageable pageable);
    Page<StockTransfer> findByProductId(UUID productId, Pageable pageable);
}
