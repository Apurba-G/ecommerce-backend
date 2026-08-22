package com.ecommerce.seller.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.seller.dto.CreateSellerRequest;
import com.ecommerce.seller.dto.SellerProfileDTO;
import com.ecommerce.seller.entity.SellerProfile;
import com.ecommerce.seller.enums.SellerStatus;
import com.ecommerce.seller.repository.SellerProfileRepository;
import com.ecommerce.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {

    private final SellerProfileRepository sellerRepository;

    @Override
    @Transactional
    public SellerProfileDTO registerSeller(CreateSellerRequest request) {
        log.info("Registering seller for userId: {}, businessName: {}", request.getUserId(), request.getBusinessName());

        if (sellerRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("Seller profile already exists for user ID: " + request.getUserId());
        }
        if (sellerRepository.findByBusinessName(request.getBusinessName()).isPresent()) {
            throw new IllegalArgumentException("Business name already taken: " + request.getBusinessName());
        }

        SellerProfile seller = SellerProfile.builder()
                .userId(request.getUserId())
                .businessName(request.getBusinessName())
                .businessType(request.getBusinessType())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .businessAddress(request.getBusinessAddress())
                .businessCity(request.getBusinessCity())
                .businessState(request.getBusinessState())
                .businessCountry(request.getBusinessCountry() != null ? request.getBusinessCountry() : "India")
                .businessPostalCode(request.getBusinessPostalCode())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfsc(request.getBankIfsc())
                .bankName(request.getBankName())
                .bankAccountHolder(request.getBankAccountHolder())
                .sellerStatus(SellerStatus.PENDING)
                .commissionRate(BigDecimal.valueOf(10.00))
                .build();

        SellerProfile saved = sellerRepository.save(seller);
        log.info("Seller registered with status PENDING, sellerId: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProfileDTO getSellerById(UUID sellerId) {
        SellerProfile seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "id", sellerId.toString()));
        return mapToDTO(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProfileDTO getSellerByUserId(UUID userId) {
        SellerProfile seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "userId", userId.toString()));
        return mapToDTO(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SellerProfileDTO> getSellersByStatus(SellerStatus status, Pageable pageable) {
        Page<SellerProfile> page = sellerRepository.findBySellerStatus(status, pageable);
        List<SellerProfileDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<SellerProfileDTO>builder()
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
    @Transactional
    public SellerProfileDTO updateSellerStatus(UUID sellerId, SellerStatus status, String rejectionReason) {
        SellerProfile seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "id", sellerId.toString()));

        seller.setSellerStatus(status);
        if (status == SellerStatus.ACTIVE) {
            seller.setIsVerified(true);
            seller.setVerifiedAt(LocalDateTime.now());
        } else if (status == SellerStatus.REJECTED || status == SellerStatus.SUSPENDED) {
            seller.setRejectionReason(rejectionReason);
            if (status == SellerStatus.SUSPENDED) {
                seller.setSuspendedAt(LocalDateTime.now());
                seller.setSuspensionReason(rejectionReason);
            }
        }

        SellerProfile updated = sellerRepository.save(seller);
        log.info("Seller ID: {} status updated to: {}", sellerId, status);
        return mapToDTO(updated);
    }

    private SellerProfileDTO mapToDTO(SellerProfile seller) {
        return SellerProfileDTO.builder()
                .id(seller.getId())
                .userId(seller.getUserId())
                .businessName(seller.getBusinessName())
                .businessType(seller.getBusinessType())
                .gstin(seller.getGstin())
                .panNumber(seller.getPanNumber())
                .businessAddress(seller.getBusinessAddress())
                .businessCity(seller.getBusinessCity())
                .businessState(seller.getBusinessState())
                .businessCountry(seller.getBusinessCountry())
                .businessPostalCode(seller.getBusinessPostalCode())
                .bankAccountNumber(seller.getBankAccountNumber())
                .bankIfsc(seller.getBankIfsc())
                .bankName(seller.getBankName())
                .bankAccountHolder(seller.getBankAccountHolder())
                .sellerStatus(seller.getSellerStatus())
                .commissionRate(seller.getCommissionRate())
                .totalRevenue(seller.getTotalRevenue())
                .totalPayouts(seller.getTotalPayouts())
                .pendingPayout(seller.getPendingPayout())
                .totalOrders(seller.getTotalOrders())
                .totalProducts(seller.getTotalProducts())
                .rating(seller.getRating())
                .reviewCount(seller.getReviewCount())
                .isVerified(seller.getIsVerified())
                .isFeatured(seller.getIsFeatured())
                .verifiedAt(seller.getVerifiedAt())
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .build();
    }
}
