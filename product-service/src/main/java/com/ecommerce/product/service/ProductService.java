package com.ecommerce.product.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.product.dto.ProductCreateRequest;
import com.ecommerce.product.dto.ProductDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    ProductDTO createProduct(UUID sellerId, ProductCreateRequest request);

    ProductDTO getProductById(UUID id);

    ProductDTO getProductBySlug(String slug);

    PagedResponse<ProductDTO> getProducts(Pageable pageable);

    PagedResponse<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable);

    PagedResponse<ProductDTO> getProductsByBrand(UUID brandId, Pageable pageable);

    PagedResponse<ProductDTO> searchProducts(String keyword, Pageable pageable);

    void deleteProduct(UUID id, UUID sellerId);
}
