package com.ecommerce.brand.service.impl;

import com.ecommerce.brand.dto.BrandCreateRequest;
import com.ecommerce.brand.dto.BrandDTO;
import com.ecommerce.brand.entity.Brand;
import com.ecommerce.brand.repository.BrandRepository;
import com.ecommerce.brand.service.BrandService;
import com.ecommerce.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDTO createBrand(BrandCreateRequest request) {
        if (brandRepository.existsByName(request.getName().trim())) {
            throw new com.ecommerce.common.exception.BusinessException(
                    com.ecommerce.common.constant.CommonErrorCode.DUPLICATE_RESOURCE,
                    "Brand with name '" + request.getName() + "' already exists"
            );
        }

        String slug = generateSlug(request.getName());
        if (brandRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .websiteUrl(request.getWebsiteUrl())
                .country(request.getCountry())
                .isFeatured(request.isFeatured())
                .isActive(request.isActive())
                .metaTitle(request.getMetaTitle())
                .metaDesc(request.getMetaDesc())
                .build();

        Brand saved = brandRepository.save(brand);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands", key = "#id")
    public BrandDTO getBrandById(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return mapToDTO(brand);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands", key = "#slug")
    public BrandDTO getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "slug", slug));
        return mapToDTO(brand);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands", key = "'all'")
    public List<BrandDTO> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandDTO> getFeaturedBrands() {
        return brandRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deleteBrand(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brand.setActive(false);
        brandRepository.save(brand);
    }

    private String generateSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    private BrandDTO mapToDTO(Brand entity) {
        return BrandDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .bannerUrl(entity.getBannerUrl())
                .websiteUrl(entity.getWebsiteUrl())
                .country(entity.getCountry())
                .isFeatured(entity.isFeatured())
                .isActive(entity.isActive())
                .metaTitle(entity.getMetaTitle())
                .metaDesc(entity.getMetaDesc())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
