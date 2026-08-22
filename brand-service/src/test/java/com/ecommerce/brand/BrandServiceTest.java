package com.ecommerce.brand;

import com.ecommerce.brand.dto.BrandCreateRequest;
import com.ecommerce.brand.dto.BrandDTO;
import com.ecommerce.brand.entity.Brand;
import com.ecommerce.brand.repository.BrandRepository;
import com.ecommerce.brand.service.impl.BrandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand sampleBrand;

    @BeforeEach
    void setUp() {
        sampleBrand = Brand.builder()
                .id(UUID.randomUUID())
                .name("Apple")
                .slug("apple")
                .isActive(true)
                .isFeatured(true)
                .build();
    }

    @Test
    @DisplayName("Should create brand and generate slug")
    void testCreateBrand() {
        BrandCreateRequest request = BrandCreateRequest.builder()
                .name("Apple")
                .country("USA")
                .build();

        when(brandRepository.existsBySlug("apple")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenReturn(sampleBrand);

        BrandDTO result = brandService.createBrand(request);

        assertNotNull(result);
        assertEquals("Apple", result.getName());
        assertEquals("apple", result.getSlug());
    }

    @Test
    @DisplayName("Should return all active brands")
    void testGetAllActiveBrands() {
        when(brandRepository.findByIsActiveTrue()).thenReturn(List.of(sampleBrand));

        List<BrandDTO> brands = brandService.getAllActiveBrands();

        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Apple", brands.get(0).getName());
    }

    @Test
    @DisplayName("Should retrieve brand by slug")
    void testGetBrandBySlug() {
        when(brandRepository.findBySlug("apple")).thenReturn(Optional.of(sampleBrand));

        BrandDTO result = brandService.getBrandBySlug("apple");

        assertNotNull(result);
        assertEquals("apple", result.getSlug());
    }
}
