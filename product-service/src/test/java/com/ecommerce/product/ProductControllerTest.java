package com.ecommerce.product;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.product.controller.ProductController;
import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    @DisplayName("GET /api/v1/products/slug/{slug} should return product")
    void testGetProductBySlug() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .id(UUID.randomUUID())
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sellingPrice(new BigDecimal("949.00"))
                .build();

        when(productService.getProductBySlug("iphone-15-pro")).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/slug/iphone-15-pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("iphone-15-pro"))
                .andExpect(jsonPath("$.data.name").value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("GET /api/v1/products should return paged list")
    void testGetProductsList() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .id(UUID.randomUUID())
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .build();

        PagedResponse<ProductDTO> pagedResponse = PagedResponse.<ProductDTO>builder()
                .content(List.of(productDTO))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(productService.getProducts(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].slug").value("iphone-15-pro"));
    }
}
