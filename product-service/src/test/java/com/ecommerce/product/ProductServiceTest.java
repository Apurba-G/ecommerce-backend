package com.ecommerce.product;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.product.dto.ProductCreateRequest;
import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventPublisher eventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID sellerId;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        sampleProduct = Product.builder()
                .id(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .sellerId(sellerId)
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sku("IPHONE-15-PRO")
                .basePrice(new BigDecimal("999.00"))
                .sellingPrice(new BigDecimal("949.00"))
                .status("ACTIVE")
                .isActive(true)
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .specifications(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create product, generate slug, and publish event")
    void testCreateProduct() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(sampleProduct.getCategoryId())
                .brandId(sampleProduct.getBrandId())
                .name("iPhone 15 Pro")
                .sku("IPHONE-15-PRO")
                .basePrice(new BigDecimal("999.00"))
                .sellingPrice(new BigDecimal("949.00"))
                .build();

        when(productRepository.existsBySlug("iphone-15-pro")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDTO result = productService.createProduct(sellerId, request);

        assertNotNull(result);
        assertEquals("iPhone 15 Pro", result.getName());
        assertEquals("iphone-15-pro", result.getSlug());
        verify(eventPublisher, times(1)).publishProductCreated(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should retrieve product by slug")
    void testGetProductBySlug() {
        when(productRepository.findBySlug("iphone-15-pro")).thenReturn(Optional.of(sampleProduct));

        ProductDTO result = productService.getProductBySlug("iphone-15-pro");

        assertNotNull(result);
        assertEquals("iPhone 15 Pro", result.getName());
    }

    @Test
    @DisplayName("Should return paged products")
    void testGetProductsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(page);

        PagedResponse<ProductDTO> response = productService.getProducts(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("iPhone 15 Pro", response.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent slug")
    void testGetProductNotFound() {
        when(productRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductBySlug("unknown-slug"));
    }
}
