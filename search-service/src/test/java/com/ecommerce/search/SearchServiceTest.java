package com.ecommerce.search;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.search.dto.SearchFilterRequest;
import com.ecommerce.search.dto.SearchProductDTO;
import com.ecommerce.search.dto.SearchSuggestionDTO;
import com.ecommerce.search.entity.SearchProduct;
import com.ecommerce.search.repository.SearchProductRepository;
import com.ecommerce.search.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchProductRepository searchProductRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    void testSearch_Success() {
        UUID prodId = UUID.randomUUID();
        SearchProduct product = SearchProduct.builder()
                .id(prodId.toString())
                .productId(prodId)
                .name("Apple iPhone 15")
                .sku("IPHONE-15")
                .sellingPrice(new BigDecimal("999.00"))
                .isActive(true)
                .inStock(true)
                .build();

        when(searchProductRepository.findAll()).thenReturn(List.of(product));

        SearchFilterRequest req = SearchFilterRequest.builder().query("iPhone").build();
        PagedResponse<SearchProductDTO> result = searchService.search(req, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Apple iPhone 15", result.getContent().get(0).getName());
    }

    @Test
    void testGetSuggestions_Success() {
        UUID prodId = UUID.randomUUID();
        SearchProduct product = SearchProduct.builder()
                .id(prodId.toString())
                .productId(prodId)
                .name("Apple iPhone 15 Pro")
                .sellingPrice(new BigDecimal("1099.00"))
                .isActive(true)
                .build();

        when(searchProductRepository.findAll()).thenReturn(List.of(product));

        List<SearchSuggestionDTO> suggestions = searchService.getSuggestions("iph", 10);

        assertNotNull(suggestions);
        assertEquals(1, suggestions.size());
        assertEquals("Apple iPhone 15 Pro", suggestions.get(0).getName());
    }
}
