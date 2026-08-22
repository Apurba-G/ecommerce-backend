package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.WarehouseCreateRequest;
import com.ecommerce.inventory.dto.WarehouseDTO;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseDTO createWarehouse(WarehouseCreateRequest request);
    WarehouseDTO getWarehouseById(UUID id);
    WarehouseDTO getWarehouseByCode(String code);
    List<WarehouseDTO> getAllActiveWarehouses();
    WarehouseDTO getDefaultWarehouse();
    WarehouseDTO updateWarehouse(UUID id, WarehouseCreateRequest request);
    void deleteWarehouse(UUID id);
}
