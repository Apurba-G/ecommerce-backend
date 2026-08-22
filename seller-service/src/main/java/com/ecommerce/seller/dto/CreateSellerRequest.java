package com.ecommerce.seller.dto;

import com.ecommerce.seller.enums.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSellerRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    private String gstin;

    private String panNumber;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    @NotBlank(message = "Business city is required")
    private String businessCity;

    @NotBlank(message = "Business state is required")
    private String businessState;

    private String businessCountry;

    private String businessPostalCode;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank IFSC code is required")
    private String bankIfsc;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account holder name is required")
    private String bankAccountHolder;
}
