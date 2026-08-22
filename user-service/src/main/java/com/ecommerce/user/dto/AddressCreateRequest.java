package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressCreateRequest {

    @Builder.Default
    private String addressType = "HOME";

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @Builder.Default
    private String country = "India";

    private String postalCode;

    @Builder.Default
    private boolean isDefault = false;

    private Double latitude;
    private Double longitude;
}
