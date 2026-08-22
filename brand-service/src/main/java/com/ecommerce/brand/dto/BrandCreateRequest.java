package com.ecommerce.brand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandCreateRequest {

    @NotBlank(message = "Brand name is required")
    private String name;

    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String websiteUrl;
    private String country;
    @Builder.Default
    private boolean isFeatured = false;
    @Builder.Default
    private boolean isActive = true;
    private String metaTitle;
    private String metaDesc;
}
