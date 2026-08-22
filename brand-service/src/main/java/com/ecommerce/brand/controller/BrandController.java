package com.ecommerce.brand.controller;

import com.ecommerce.brand.dto.BrandCreateRequest;
import com.ecommerce.brand.dto.BrandDTO;
import com.ecommerce.brand.service.BrandService;
import com.ecommerce.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<ApiResponse<BrandDTO>> createBrand(@Valid @RequestBody BrandCreateRequest request) {
        BrandDTO created = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Brand created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getAllBrands() {
        List<BrandDTO> brands = brandService.getAllActiveBrands();
        return ResponseEntity.ok(ApiResponse.success(brands, "Brands retrieved successfully"));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getFeaturedBrands() {
        List<BrandDTO> featured = brandService.getFeaturedBrands();
        return ResponseEntity.ok(ApiResponse.success(featured, "Featured brands retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandById(@PathVariable("id") UUID id) {
        BrandDTO brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success(brand, "Brand retrieved successfully"));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandBySlug(@PathVariable("slug") String slug) {
        BrandDTO brand = brandService.getBrandBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(brand, "Brand retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable("id") UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Brand deleted successfully"));
    }
}
