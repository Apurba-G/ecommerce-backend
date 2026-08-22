package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByProductId(UUID productId);

    List<Inventory> findByProductIdAndVariantId(UUID productId, UUID variantId);

    Optional<Inventory> findByProductIdAndWarehouseIdAndVariantIdIsNull(UUID productId, UUID warehouseId);

    Optional<Inventory> findByProductIdAndVariantIdAndWarehouseId(UUID productId, UUID variantId, UUID warehouseId);

    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND (i.variantId = :variantId OR (:variantId IS NULL AND i.variantId IS NULL))")
    List<Inventory> findStockByProductAndVariant(@Param("productId") UUID productId, @Param("variantId") UUID variantId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.lowStockThreshold AND i.trackInventory = true")
    List<Inventory> findLowStockInventories();

    Page<Inventory> findByWarehouseId(UUID warehouseId, Pageable pageable);
}
