package com.ecommerce.category.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID parentId;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private String bannerUrl;
    private String iconUrl;
    private boolean isFeatured;
    private boolean isActive;
    private int sortOrder;
    private int level;
    private String path;
    private String metaTitle;
    private String metaDesc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryDTO> children;
}
