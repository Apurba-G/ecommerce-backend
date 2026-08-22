package com.ecommerce.product.dto;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String imageUrl;
    private String altText;
    private boolean isPrimary;
    private int sortOrder;
}
