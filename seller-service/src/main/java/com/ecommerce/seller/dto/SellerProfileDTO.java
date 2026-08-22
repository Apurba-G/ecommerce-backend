package com.ecommerce.seller.dto;

import com.ecommerce.seller.enums.BusinessType;
import com.ecommerce.seller.enums.SellerStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfileDTO {
    private UUID id;
    private UUID userId;
    private String businessName;
    private BusinessType businessType;
    private String gstin;
    private String panNumber;
    private String businessAddress;
    private String businessCity;
    private String businessState;
    private String businessCountry;
    private String businessPostalCode;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
    private String bankAccountHolder;
    private SellerStatus sellerStatus;
    private BigDecimal commissionRate;
    private BigDecimal totalRevenue;
    private BigDecimal totalPayouts;
    private BigDecimal pendingPayout;
    private Integer totalOrders;
    private Integer totalProducts;
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean isVerified;
    private Boolean isFeatured;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
