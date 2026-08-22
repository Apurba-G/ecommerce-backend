package com.ecommerce.inventory.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.inventory.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryDTO adjustStock(UUID sellerId, InventoryStockAdjustmentRequest request);
    InventoryDTO reserveStock(InventoryReserveRequest request);
    InventoryDTO releaseStock(InventoryReserveRequest request);
    InventoryDTO deductReservedStock(InventoryReserveRequest request);
    InventoryDTO getStockByProductAndWarehouse(UUID productId, UUID variantId, UUID warehouseId);
    List<InventoryDTO> getStockByProduct(UUID productId, UUID variantId);
    int getTotalAvailableStock(UUID productId, UUID variantId);
    PagedResponse<InventoryTransactionDTO> getInventoryTransactions(UUID inventoryId, Pageable pageable);
    List<StockAlertDTO> getUnresolvedStockAlerts();
    StockAlertDTO resolveStockAlert(UUID alertId);
}
