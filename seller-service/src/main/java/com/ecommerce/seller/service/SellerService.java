package com.ecommerce.seller.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.seller.dto.CreateSellerRequest;
import com.ecommerce.seller.dto.SellerProfileDTO;
import com.ecommerce.seller.enums.SellerStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SellerService {

    SellerProfileDTO registerSeller(CreateSellerRequest request);

    SellerProfileDTO getSellerById(UUID sellerId);

    SellerProfileDTO getSellerByUserId(UUID userId);

    PagedResponse<SellerProfileDTO> getSellersByStatus(SellerStatus status, Pageable pageable);

    SellerProfileDTO updateSellerStatus(UUID sellerId, SellerStatus status, String rejectionReason);
}
