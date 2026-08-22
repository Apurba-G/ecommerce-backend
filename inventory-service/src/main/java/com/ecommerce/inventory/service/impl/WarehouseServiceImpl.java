package com.ecommerce.inventory.service.impl;

import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.dto.WarehouseCreateRequest;
import com.ecommerce.inventory.dto.WarehouseDTO;
import com.ecommerce.inventory.entity.Warehouse;
import com.ecommerce.inventory.repository.WarehouseRepository;
import com.ecommerce.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    private WarehouseDTO mapToDTO(Warehouse w) {
        return WarehouseDTO.builder()
                .id(w.getId())
                .name(w.getName())
                .code(w.getCode())
                .addressLine1(w.getAddressLine1())
                .addressLine2(w.getAddressLine2())
                .city(w.getCity())
                .state(w.getState())
                .country(w.getCountry())
                .postalCode(w.getPostalCode())
                .latitude(w.getLatitude())
                .longitude(w.getLongitude())
                .managerName(w.getManagerName())
                .managerEmail(w.getManagerEmail())
                .managerPhone(w.getManagerPhone())
                .isActive(w.getIsActive())
                .isDefault(w.getIsDefault())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseDTO createWarehouse(WarehouseCreateRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE, "Warehouse with code '" + request.getCode() + "' already exists");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            warehouseRepository.findByIsDefaultTrue().ifPresent(w -> {
                w.setIsDefault(false);
                warehouseRepository.save(w);
            });
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase().trim())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .managerName(request.getManagerName())
                .managerEmail(request.getManagerEmail())
                .managerPhone(request.getManagerPhone())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Created warehouse '{}' with code '{}'", saved.getName(), saved.getCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseById(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        return mapToDTO(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseByCode(String code) {
        Warehouse warehouse = warehouseRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "code", code));
        return mapToDTO(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouses", key = "'active'")
    public List<WarehouseDTO> getAllActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getDefaultWarehouse() {
        Warehouse warehouse = warehouseRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Default Warehouse not configured"));
        return mapToDTO(warehouse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseDTO updateWarehouse(UUID id, WarehouseCreateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

        if (!warehouse.getCode().equalsIgnoreCase(request.getCode()) && warehouseRepository.existsByCode(request.getCode())) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE, "Warehouse with code '" + request.getCode() + "' already exists");
        }

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(warehouse.getIsDefault())) {
            warehouseRepository.findByIsDefaultTrue().ifPresent(w -> {
                w.setIsDefault(false);
                warehouseRepository.save(w);
            });
        }

        warehouse.setName(request.getName());
        warehouse.setCode(request.getCode().toUpperCase().trim());
        warehouse.setAddressLine1(request.getAddressLine1());
        warehouse.setAddressLine2(request.getAddressLine2());
        warehouse.setCity(request.getCity());
        warehouse.setState(request.getState());
        warehouse.setCountry(request.getCountry());
        warehouse.setPostalCode(request.getPostalCode());
        warehouse.setLatitude(request.getLatitude());
        warehouse.setLongitude(request.getLongitude());
        warehouse.setManagerName(request.getManagerName());
        warehouse.setManagerEmail(request.getManagerEmail());
        warehouse.setManagerPhone(request.getManagerPhone());
        warehouse.setIsActive(request.getIsActive() != null ? request.getIsActive() : warehouse.getIsActive());
        warehouse.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : warehouse.getIsDefault());

        Warehouse updated = warehouseRepository.save(warehouse);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public void deleteWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
        log.info("Deactivated warehouse id '{}'", id);
    }
}
