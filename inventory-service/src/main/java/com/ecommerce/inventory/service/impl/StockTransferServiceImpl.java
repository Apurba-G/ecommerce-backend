package com.ecommerce.inventory.service.impl;

import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.inventory.dto.InventoryStockAdjustmentRequest;
import com.ecommerce.inventory.dto.StockTransferCreateRequest;
import com.ecommerce.inventory.dto.StockTransferDTO;
import com.ecommerce.inventory.entity.StockTransfer;
import com.ecommerce.inventory.entity.Warehouse;
import com.ecommerce.inventory.repository.StockTransferRepository;
import com.ecommerce.inventory.repository.WarehouseRepository;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.inventory.service.StockTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;

    private StockTransferDTO mapToDTO(StockTransfer t) {
        return StockTransferDTO.builder()
                .id(t.getId())
                .fromWarehouseId(t.getFromWarehouse().getId())
                .fromWarehouseName(t.getFromWarehouse().getName())
                .toWarehouseId(t.getToWarehouse().getId())
                .toWarehouseName(t.getToWarehouse().getName())
                .productId(t.getProductId())
                .variantId(t.getVariantId())
                .quantity(t.getQuantity())
                .status(t.getStatus())
                .notes(t.getNotes())
                .initiatedBy(t.getInitiatedBy())
                .completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public StockTransferDTO initiateTransfer(UUID initiatorId, StockTransferCreateRequest request) {
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Source and destination warehouses cannot be the same");
        }

        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source Warehouse", "id", request.getFromWarehouseId()));
        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination Warehouse", "id", request.getToWarehouseId()));

        // Reduce from source warehouse
        inventoryService.adjustStock(initiatorId, InventoryStockAdjustmentRequest.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .warehouseId(fromWarehouse.getId())
                .quantity(-request.getQuantity())
                .transactionType("TRANSFER_OUT")
                .referenceType("TRANSFER")
                .note("Transfer out to " + toWarehouse.getName())
                .build());

        StockTransfer transfer = StockTransfer.builder()
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .quantity(request.getQuantity())
                .status("IN_TRANSIT")
                .notes(request.getNotes())
                .initiatedBy(initiatorId)
                .build();

        StockTransfer saved = stockTransferRepository.save(transfer);
        log.info("Initiated stock transfer id {} from {} to {}", saved.getId(), fromWarehouse.getCode(), toWarehouse.getCode());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public StockTransferDTO completeTransfer(UUID transferId) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", transferId));

        if (!"IN_TRANSIT".equals(transfer.getStatus()) && !"PENDING".equals(transfer.getStatus())) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Transfer is already in " + transfer.getStatus() + " status");
        }

        // Add to destination warehouse
        inventoryService.adjustStock(transfer.getInitiatedBy(), InventoryStockAdjustmentRequest.builder()
                .productId(transfer.getProductId())
                .variantId(transfer.getVariantId())
                .warehouseId(transfer.getToWarehouse().getId())
                .quantity(transfer.getQuantity())
                .transactionType("TRANSFER_IN")
                .referenceType("TRANSFER")
                .note("Transfer received from " + transfer.getFromWarehouse().getName())
                .build());

        transfer.setStatus("COMPLETED");
        transfer.setCompletedAt(LocalDateTime.now());
        StockTransfer saved = stockTransferRepository.save(transfer);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public StockTransferDTO cancelTransfer(UUID transferId) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", transferId));

        if (!"IN_TRANSIT".equals(transfer.getStatus()) && !"PENDING".equals(transfer.getStatus())) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Transfer cannot be cancelled in status " + transfer.getStatus());
        }

        // Return stock back to source warehouse
        inventoryService.adjustStock(transfer.getInitiatedBy(), InventoryStockAdjustmentRequest.builder()
                .productId(transfer.getProductId())
                .variantId(transfer.getVariantId())
                .warehouseId(transfer.getFromWarehouse().getId())
                .quantity(transfer.getQuantity())
                .transactionType("TRANSFER_CANCELLED")
                .referenceType("TRANSFER")
                .note("Transfer cancelled, returned to source")
                .build());

        transfer.setStatus("CANCELLED");
        StockTransfer saved = stockTransferRepository.save(transfer);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StockTransferDTO> getTransfers(Pageable pageable) {
        Page<StockTransfer> page = stockTransferRepository.findAll(pageable);
        List<StockTransferDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<StockTransferDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }
}
