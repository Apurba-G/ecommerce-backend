package com.ecommerce.wishlist.dto;

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
public class WishlistCreateRequest {

    @NotBlank(message = "Wishlist name is required")
    @Size(max = 255, message = "Wishlist name cannot exceed 255 characters")
    private String name;

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Boolean isPublic = false;
}
