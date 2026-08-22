package com.ecommerce.inventory;

import com.ecommerce.inventory.dto.InventoryDTO;
import com.ecommerce.inventory.dto.InventoryReserveRequest;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.Warehouse;
import com.ecommerce.inventory.event.InventoryEventPublisher;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.InventoryTransactionRepository;
import com.ecommerce.inventory.repository.StockAlertRepository;
import com.ecommerce.inventory.repository.WarehouseRepository;
import com.ecommerce.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private StockAlertRepository alertRepository;
    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void testReserveStock_Success() {
        UUID productId = UUID.randomUUID();
        Warehouse wh = Warehouse.builder().id(UUID.randomUUID()).name("Main Hub").code("WH-01").build();

        Inventory inv = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .warehouse(wh)
                .quantity(100)
                .reservedQuantity(10)
                .trackInventory(true)
                .build();

        when(inventoryRepository.findStockByProductAndVariant(productId, null)).thenReturn(List.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inv);

        InventoryReserveRequest req = InventoryReserveRequest.builder()
                .productId(productId)
                .quantity(5)
                .referenceId("ORD-101")
                .build();

        InventoryDTO result = inventoryService.reserveStock(req);

        assertNotNull(result);
        assertEquals(15, inv.getReservedQuantity());
        verify(transactionRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishStockUpdated(any(), any(), anyInt());
    }
}
