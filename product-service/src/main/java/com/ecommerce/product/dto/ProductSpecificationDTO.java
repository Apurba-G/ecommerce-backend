package com.ecommerce.product.dto;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSpecificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String specKey;
    private String specValue;
    private String specGroup;
    private int sortOrder;
}
