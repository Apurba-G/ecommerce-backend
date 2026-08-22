package com.ecommerce.inventory.service.impl;

import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryTransaction;
import com.ecommerce.inventory.entity.StockAlert;
import com.ecommerce.inventory.entity.Warehouse;
import com.ecommerce.inventory.event.InventoryEventPublisher;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.InventoryTransactionRepository;
import com.ecommerce.inventory.repository.StockAlertRepository;
import com.ecommerce.inventory.repository.WarehouseRepository;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final StockAlertRepository alertRepository;
    private final InventoryEventPublisher eventPublisher;

    private InventoryDTO mapToDTO(Inventory i) {
        return InventoryDTO.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .variantId(i.getVariantId())
                .warehouseId(i.getWarehouse().getId())
                .warehouseName(i.getWarehouse().getName())
                .warehouseCode(i.getWarehouse().getCode())
                .sellerId(i.getSellerId())
                .quantity(i.getQuantity())
                .reservedQuantity(i.getReservedQuantity())
                .availableQuantity(i.getAvailableQuantity())
                .soldQuantity(i.getSoldQuantity())
                .lowStockThreshold(i.getLowStockThreshold())
                .trackInventory(i.getTrackInventory())
                .batchNumber(i.getBatchNumber())
                .expiryDate(i.getExpiryDate())
                .version(i.getVersion())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private InventoryTransactionDTO mapTransactionToDTO(InventoryTransaction t) {
        return InventoryTransactionDTO.builder()
                .id(t.getId())
                .inventoryId(t.getInventory().getId())
                .transactionType(t.getTransactionType())
                .quantity(t.getQuantity())
                .quantityBefore(t.getQuantityBefore())
                .quantityAfter(t.getQuantityAfter())
                .referenceType(t.getReferenceType())
                .referenceId(t.getReferenceId())
                .note(t.getNote())
                .performedBy(t.getPerformedBy())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private StockAlertDTO mapAlertToDTO(StockAlert a) {
        return StockAlertDTO.builder()
                .id(a.getId())
                .inventoryId(a.getInventory().getId())
                .productId(a.getInventory().getProductId())
                .alertType(a.getAlertType())
                .thresholdQuantity(a.getThresholdQuantity())
                .currentQuantity(a.getCurrentQuantity())
                .isResolved(a.getIsResolved())
                .resolvedAt(a.getResolvedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "stock", allEntries = true)
    public InventoryDTO adjustStock(UUID sellerId, InventoryStockAdjustmentRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));

        Optional<Inventory> existingOpt = request.getVariantId() != null
                ? inventoryRepository.findByProductIdAndVariantIdAndWarehouseId(request.getProductId(), request.getVariantId(), request.getWarehouseId())
                : inventoryRepository.findByProductIdAndWarehouseIdAndVariantIdIsNull(request.getProductId(), request.getWarehouseId());

        Inventory inventory = existingOpt.orElseGet(() -> Inventory.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .warehouse(warehouse)
                .sellerId(sellerId)
                .quantity(0)
                .reservedQuantity(0)
                .soldQuantity(0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .trackInventory(true)
                .batchNumber(request.getBatchNumber())
                .expiryDate(request.getExpiryDate())
                .build());

        int before = inventory.getQuantity();
        int adjustQty = request.getQuantity();
        int after = before + adjustQty;

        if (after < inventory.getReservedQuantity()) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK, "Cannot reduce inventory below currently reserved quantity (" + inventory.getReservedQuantity() + ")");
        }

        inventory.setQuantity(after);
        if (request.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getBatchNumber() != null) {
            inventory.setBatchNumber(request.getBatchNumber());
        }
        if (request.getExpiryDate() != null) {
            inventory.setExpiryDate(request.getExpiryDate());
        }

        Inventory saved = inventoryRepository.save(inventory);

        // Record Audit Transaction
        InventoryTransaction tx = InventoryTransaction.builder()
                .inventory(saved)
                .transactionType(request.getTransactionType())
                .quantity(adjustQty)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .note(request.getNote())
                .performedBy(sellerId)
                .build();
        transactionRepository.save(tx);

        // Stock Alert Check
        if (after <= saved.getLowStockThreshold()) {
            StockAlert alert = StockAlert.builder()
                    .inventory(saved)
                    .alertType(after == 0 ? "OUT_OF_STOCK" : "LOW_STOCK")
                    .thresholdQuantity(saved.getLowStockThreshold())
                    .currentQuantity(after)
                    .isResolved(false)
                    .build();
            alertRepository.save(alert);
        }

        // Publish event to RabbitMQ
        eventPublisher.publishStockUpdated(saved.getProductId(), saved.getVariantId(), saved.getAvailableQuantity());

        log.info("Stock adjusted for product {} in warehouse {}: before={}, after={}", request.getProductId(), warehouse.getCode(), before, after);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stock", allEntries = true)
    public InventoryDTO reserveStock(InventoryReserveRequest request) {
        List<Inventory> list = inventoryRepository.findStockByProductAndVariant(request.getProductId(), request.getVariantId());
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Inventory not found for product id: " + request.getProductId());
        }

        // Pick specific warehouse or warehouse with highest stock
        Inventory target = request.getWarehouseId() != null
                ? list.stream().filter(i -> i.getWarehouse().getId().equals(request.getWarehouseId())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"))
                : list.stream().filter(i -> i.getAvailableQuantity() >= request.getQuantity()).findFirst()
                    .orElseThrow(() -> new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK, "Insufficient stock available to reserve " + request.getQuantity() + " units"));

        if (target.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK, "Only " + target.getAvailableQuantity() + " units available to reserve");
        }

        int before = target.getQuantity();
        target.setReservedQuantity(target.getReservedQuantity() + request.getQuantity());
        Inventory saved = inventoryRepository.save(target);

        InventoryTransaction tx = InventoryTransaction.builder()
                .inventory(saved)
                .transactionType("RESERVE")
                .quantity(request.getQuantity())
                .quantityBefore(before)
                .quantityAfter(before)
                .referenceType("ORDER_RESERVATION")
                .referenceId(request.getReferenceId())
                .note("Reserved stock for order/cart")
                .build();
        transactionRepository.save(tx);

        eventPublisher.publishStockUpdated(saved.getProductId(), saved.getVariantId(), saved.getAvailableQuantity());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stock", allEntries = true)
    public InventoryDTO releaseStock(InventoryReserveRequest request) {
        List<Inventory> list = inventoryRepository.findStockByProductAndVariant(request.getProductId(), request.getVariantId());
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Inventory not found for product id: " + request.getProductId());
        }

        Inventory target = request.getWarehouseId() != null
                ? list.stream().filter(i -> i.getWarehouse().getId().equals(request.getWarehouseId())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"))
                : list.stream().filter(i -> i.getReservedQuantity() >= request.getQuantity()).findFirst()
                    .orElse(list.get(0));

        int newReserved = Math.max(0, target.getReservedQuantity() - request.getQuantity());
        target.setReservedQuantity(newReserved);
        Inventory saved = inventoryRepository.save(target);

        InventoryTransaction tx = InventoryTransaction.builder()
                .inventory(saved)
                .transactionType("RELEASE")
                .quantity(request.getQuantity())
                .quantityBefore(saved.getQuantity())
                .quantityAfter(saved.getQuantity())
                .referenceType("RESERVATION_CANCELLED")
                .referenceId(request.getReferenceId())
                .note("Released reserved stock")
                .build();
        transactionRepository.save(tx);

        eventPublisher.publishStockUpdated(saved.getProductId(), saved.getVariantId(), saved.getAvailableQuantity());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stock", allEntries = true)
    public InventoryDTO deductReservedStock(InventoryReserveRequest request) {
        List<Inventory> list = inventoryRepository.findStockByProductAndVariant(request.getProductId(), request.getVariantId());
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Inventory not found for product id: " + request.getProductId());
        }

        Inventory target = request.getWarehouseId() != null
                ? list.stream().filter(i -> i.getWarehouse().getId().equals(request.getWarehouseId())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"))
                : list.stream().filter(i -> i.getReservedQuantity() >= request.getQuantity()).findFirst()
                    .orElseThrow(() -> new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK, "No active reservation found to deduct"));

        int before = target.getQuantity();
        int after = before - request.getQuantity();
        if (after < 0) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK, "Cannot deduct more than total stock");
        }

        target.setQuantity(after);
        target.setReservedQuantity(Math.max(0, target.getReservedQuantity() - request.getQuantity()));
        target.setSoldQuantity(target.getSoldQuantity() + request.getQuantity());
        Inventory saved = inventoryRepository.save(target);

        InventoryTransaction tx = InventoryTransaction.builder()
                .inventory(saved)
                .transactionType("DEDUCT_ORDER")
                .quantity(request.getQuantity())
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceType("ORDER_PAID")
                .referenceId(request.getReferenceId())
                .note("Deducted stock on payment confirmation")
                .build();
        transactionRepository.save(tx);

        eventPublisher.publishStockUpdated(saved.getProductId(), saved.getVariantId(), saved.getAvailableQuantity());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getStockByProductAndWarehouse(UUID productId, UUID variantId, UUID warehouseId) {
        Optional<Inventory> opt = variantId != null
                ? inventoryRepository.findByProductIdAndVariantIdAndWarehouseId(productId, variantId, warehouseId)
                : inventoryRepository.findByProductIdAndWarehouseIdAndVariantIdIsNull(productId, warehouseId);

        return opt.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getStockByProduct(UUID productId, UUID variantId) {
        return inventoryRepository.findStockByProductAndVariant(productId, variantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "stock", key = "#productId.toString() + '-' + (#variantId != null ? #variantId.toString() : 'null')")
    public int getTotalAvailableStock(UUID productId, UUID variantId) {
        return inventoryRepository.findStockByProductAndVariant(productId, variantId).stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionDTO> getInventoryTransactions(UUID inventoryId, Pageable pageable) {
        Page<InventoryTransaction> page = transactionRepository.findByInventoryId(inventoryId, pageable);
        List<InventoryTransactionDTO> dtos = page.getContent().stream().map(this::mapTransactionToDTO).collect(Collectors.toList());
        return PagedResponse.<InventoryTransactionDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockAlertDTO> getUnresolvedStockAlerts() {
        return alertRepository.findByIsResolvedFalse().stream()
                .map(this::mapAlertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockAlertDTO resolveStockAlert(UUID alertId) {
        StockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("StockAlert", "id", alertId));
        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        StockAlert saved = alertRepository.save(alert);
        return mapAlertToDTO(saved);
    }
}
