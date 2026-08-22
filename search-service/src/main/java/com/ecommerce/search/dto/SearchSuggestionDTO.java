package com.ecommerce.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchSuggestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID productId;
    private String name;
    private String slug;
    private String categoryName;
    private String brandName;
    private String primaryImage;
    private Double price;
}
