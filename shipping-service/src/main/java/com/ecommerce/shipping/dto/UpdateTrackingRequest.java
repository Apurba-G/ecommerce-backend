package com.ecommerce.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTrackingRequest {

    @NotBlank(message = "Tracking status is required")
    private String status;

    private String location;

    private String description;

    private String activityCode;
}
