package com.ecommerce.brand;

import com.ecommerce.brand.controller.BrandController;
import com.ecommerce.brand.dto.BrandDTO;
import com.ecommerce.brand.service.BrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BrandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private BrandController brandController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(brandController).build();
    }

    @Test
    @DisplayName("GET /api/v1/brands should return active brands")
    void testGetAllBrands() throws Exception {
        BrandDTO dto = BrandDTO.builder()
                .id(UUID.randomUUID())
                .name("Apple")
                .slug("apple")
                .country("USA")
                .isActive(true)
                .build();

        when(brandService.getAllActiveBrands()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("apple"))
                .andExpect(jsonPath("$.data[0].name").value("Apple"));
    }
}
