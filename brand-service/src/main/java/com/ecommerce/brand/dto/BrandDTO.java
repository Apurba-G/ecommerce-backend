package com.ecommerce.brand.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String websiteUrl;
    private String country;
    private boolean isFeatured;
    private boolean isActive;
    private String metaTitle;
    private String metaDesc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
