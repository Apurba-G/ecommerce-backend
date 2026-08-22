package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    Page<InventoryTransaction> findByInventoryId(UUID inventoryId, Pageable pageable);
    Page<InventoryTransaction> findByTransactionType(String transactionType, Pageable pageable);
}
