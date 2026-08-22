package com.ecommerce.inventory.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.inventory.dto.StockTransferCreateRequest;
import com.ecommerce.inventory.dto.StockTransferDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StockTransferService {
    StockTransferDTO initiateTransfer(UUID initiatorId, StockTransferCreateRequest request);
    StockTransferDTO completeTransfer(UUID transferId);
    StockTransferDTO cancelTransfer(UUID transferId);
    PagedResponse<StockTransferDTO> getTransfers(Pageable pageable);
}
