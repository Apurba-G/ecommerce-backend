package com.ecommerce.product.service.impl;

import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductImage;
import com.ecommerce.product.entity.ProductSpecification;
import com.ecommerce.product.entity.ProductVariant;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(UUID sellerId, ProductCreateRequest request) {
        String slug = generateSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Product product = Product.builder()
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .sellerId(sellerId)
                .name(request.getName())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .sku(request.getSku() != null ? request.getSku() : "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .barcode(request.getBarcode())
                .basePrice(request.getBasePrice())
                .sellingPrice(request.getSellingPrice())
                .discountPercentage(request.getDiscountPercentage())
                .taxPercentage(request.getTaxPercentage())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .isFeatured(request.isFeatured())
                .isReturnable(request.isReturnable())
                .returnPeriodDays(request.getReturnPeriodDays())
                .build();

        if (request.getVariants() != null) {
            for (ProductCreateRequest.VariantCreateRequest vReq : request.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .name(vReq.getName())
                        .sku(vReq.getSku())
                        .attributes(vReq.getAttributes())
                        .price(vReq.getPrice())
                        .sellingPrice(vReq.getSellingPrice())
                        .imageUrl(vReq.getImageUrl())
                        .sortOrder(vReq.getSortOrder())
                        .build();
                product.getVariants().add(variant);
            }
        }

        if (request.getImages() != null) {
            for (ProductCreateRequest.ImageCreateRequest imgReq : request.getImages()) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(imgReq.getImageUrl())
                        .altText(imgReq.getAltText())
                        .isPrimary(imgReq.isPrimary())
                        .sortOrder(imgReq.getSortOrder())
                        .build();
                product.getImages().add(image);
            }
        }

        if (request.getSpecifications() != null) {
            for (ProductCreateRequest.SpecificationCreateRequest sReq : request.getSpecifications()) {
                ProductSpecification spec = ProductSpecification.builder()
                        .product(product)
                        .specKey(sReq.getSpecKey())
                        .specValue(sReq.getSpecValue())
                        .specGroup(sReq.getSpecGroup())
                        .sortOrder(sReq.getSortOrder())
                        .build();
                product.getSpecifications().add(spec);
            }
        }

        Product saved = productRepository.save(product);
        eventPublisher.publishProductCreated(saved.getId(), saved.getName(), saved.getSlug(), saved.getSku());

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#slug")
    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> getProducts(Pageable pageable) {
        Page<Product> page = productRepository.findByIsActiveTrue(pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> getProductsByBrand(UUID brandId, Pageable pageable) {
        Page<Product> page = productRepository.findByBrandIdAndIsActiveTrue(brandId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        Page<Product> page = productRepository.searchByKeyword(keyword, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(UUID id, UUID sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Not authorized to delete this product");
        }

        product.setActive(false);
        product.setStatus("DISCONTINUED");
        productRepository.save(product);
    }

    private String generateSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    private ProductDTO mapToDTO(Product entity) {
        List<ProductVariantDTO> variants = entity.getVariants().stream()
                .map(v -> ProductVariantDTO.builder()
                        .id(v.getId())
                        .name(v.getName())
                        .sku(v.getSku())
                        .attributes(v.getAttributes())
                        .price(v.getPrice())
                        .sellingPrice(v.getSellingPrice())
                        .imageUrl(v.getImageUrl())
                        .isActive(v.isActive())
                        .sortOrder(v.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        List<ProductImageDTO> images = entity.getImages().stream()
                .map(i -> ProductImageDTO.builder()
                        .id(i.getId())
                        .imageUrl(i.getImageUrl())
                        .altText(i.getAltText())
                        .isPrimary(i.isPrimary())
                        .sortOrder(i.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        List<ProductSpecificationDTO> specs = entity.getSpecifications().stream()
                .map(s -> ProductSpecificationDTO.builder()
                        .id(s.getId())
                        .specKey(s.getSpecKey())
                        .specValue(s.getSpecValue())
                        .specGroup(s.getSpecGroup())
                        .sortOrder(s.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return ProductDTO.builder()
                .id(entity.getId())
                .categoryId(entity.getCategoryId())
                .brandId(entity.getBrandId())
                .sellerId(entity.getSellerId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .shortDescription(entity.getShortDescription())
                .description(entity.getDescription())
                .sku(entity.getSku())
                .barcode(entity.getBarcode())
                .basePrice(entity.getBasePrice())
                .sellingPrice(entity.getSellingPrice())
                .discountPercentage(entity.getDiscountPercentage())
                .taxPercentage(entity.getTaxPercentage())
                .status(entity.getStatus())
                .isFeatured(entity.isFeatured())
                .isActive(entity.isActive())
                .isReturnable(entity.isReturnable())
                .returnPeriodDays(entity.getReturnPeriodDays())
                .viewCount(entity.getViewCount())
                .averageRating(entity.getAverageRating())
                .reviewCount(entity.getReviewCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .variants(variants)
                .images(images)
                .specifications(specs)
                .build();
    }

    private PagedResponse<ProductDTO> mapToPagedResponse(Page<Product> page) {
        List<ProductDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<ProductDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
