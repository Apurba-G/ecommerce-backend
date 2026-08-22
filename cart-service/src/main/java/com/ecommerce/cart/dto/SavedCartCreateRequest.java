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
public class SavedCartCreateRequest {

    @NotBlank(message = "Saved cart name is required (e.g. 'Monthly Groceries')")
    private String name;
}
