package com.ecommerce.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequest {

    private UUID parentId;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
    private String imageUrl;
    private String bannerUrl;
    private String iconUrl;
    @Builder.Default
    private boolean isFeatured = false;
    @Builder.Default
    private boolean isActive = true;
    @Builder.Default
    private int sortOrder = 0;
    private String metaTitle;
    private String metaDesc;
}
