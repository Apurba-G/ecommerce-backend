package com.ecommerce.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartMergeRequest {

    @NotBlank(message = "Guest session ID is required to merge")
    private String guestSessionId;
}
