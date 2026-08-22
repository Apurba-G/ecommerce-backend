package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseCreateRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Warehouse code cannot exceed 50 characters")
    private String code;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    private String postalCode;
    private Double latitude;
    private Double longitude;

    private String managerName;
    private String managerEmail;
    private String managerPhone;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDefault = false;
}
