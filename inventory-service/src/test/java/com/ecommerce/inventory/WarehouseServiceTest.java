package com.ecommerce.inventory;

import com.ecommerce.inventory.dto.WarehouseCreateRequest;
import com.ecommerce.inventory.dto.WarehouseDTO;
import com.ecommerce.inventory.entity.Warehouse;
import com.ecommerce.inventory.repository.WarehouseRepository;
import com.ecommerce.inventory.service.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    @Test
    void testCreateWarehouse_Success() {
        WarehouseCreateRequest req = WarehouseCreateRequest.builder()
                .name("Chicago Main")
                .code("WH-CHI-01")
                .addressLine1("100 Main St")
                .city("Chicago")
                .country("USA")
                .isDefault(true)
                .build();

        Warehouse warehouse = Warehouse.builder()
                .id(UUID.randomUUID())
                .name(req.getName())
                .code(req.getCode())
                .addressLine1(req.getAddressLine1())
                .city(req.getCity())
                .country(req.getCountry())
                .isDefault(true)
                .isActive(true)
                .build();

        when(warehouseRepository.existsByCode(any())).thenReturn(false);
        when(warehouseRepository.findByIsDefaultTrue()).thenReturn(Optional.empty());
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        WarehouseDTO result = warehouseService.createWarehouse(req);

        assertNotNull(result);
        assertEquals("WH-CHI-01", result.getCode());
        assertTrue(result.getIsDefault());
    }
}
