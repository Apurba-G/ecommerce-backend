package com.ecommerce.brand.service;

import com.ecommerce.brand.dto.BrandCreateRequest;
import com.ecommerce.brand.dto.BrandDTO;

import java.util.List;
import java.util.UUID;

public interface BrandService {

    BrandDTO createBrand(BrandCreateRequest request);

    BrandDTO getBrandById(UUID id);

    BrandDTO getBrandBySlug(String slug);

    List<BrandDTO> getAllActiveBrands();

    List<BrandDTO> getFeaturedBrands();

    void deleteBrand(UUID id);
}
