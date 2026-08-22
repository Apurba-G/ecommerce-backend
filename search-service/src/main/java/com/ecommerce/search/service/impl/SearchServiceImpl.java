package com.ecommerce.search.service.impl;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.search.dto.SearchFilterRequest;
import com.ecommerce.search.dto.SearchProductDTO;
import com.ecommerce.search.dto.SearchSuggestionDTO;
import com.ecommerce.search.entity.SearchProduct;
import com.ecommerce.search.repository.SearchProductRepository;
import com.ecommerce.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchProductRepository searchProductRepository;

    private SearchProductDTO mapToDTO(SearchProduct p) {
        return SearchProductDTO.builder()
                .id(p.getProductId() != null ? p.getProductId() : (p.getId() != null ? UUID.fromString(p.getId()) : null))
                .productId(p.getProductId())
                .name(p.getName())
                .sku(p.getSku())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .brandId(p.getBrandId())
                .brandName(p.getBrandName())
                .basePrice(p.getBasePrice())
                .sellingPrice(p.getSellingPrice())
                .primaryImage(p.getPrimaryImage())
                .isActive(p.getIsActive())
                .inStock(p.getInStock())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private SearchSuggestionDTO mapToSuggestionDTO(SearchProduct p) {
        return SearchSuggestionDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .categoryName(p.getCategoryName())
                .brandName(p.getBrandName())
                .primaryImage(p.getPrimaryImage())
                .price(p.getSellingPrice() != null ? p.getSellingPrice().doubleValue() : 0.0)
                .build();
    }

    @Override
    @Cacheable(value = "search", key = "#request.toString() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResponse<SearchProductDTO> search(SearchFilterRequest request, Pageable pageable) {
        String q = request.getQuery() != null ? request.getQuery().trim().toLowerCase() : "";

        Iterable<SearchProduct> allProducts = searchProductRepository.findAll();
        List<SearchProduct> filtered = StreamSupport.stream(allProducts.spliterator(), false)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .filter(p -> q.isEmpty() ||
                        (p.getName() != null && p.getName().toLowerCase().contains(q)) ||
                        (p.getSku() != null && p.getSku().toLowerCase().contains(q)) ||
                        (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(q)) ||
                        (p.getBrandName() != null && p.getBrandName().toLowerCase().contains(q)))
                .filter(p -> request.getCategoryId() == null || request.getCategoryId().equals(p.getCategoryId()))
                .filter(p -> request.getBrandId() == null || request.getBrandId().equals(p.getBrandId()))
                .filter(p -> request.getMinPrice() == null || (p.getSellingPrice() != null && p.getSellingPrice().compareTo(request.getMinPrice()) >= 0))
                .filter(p -> request.getMaxPrice() == null || (p.getSellingPrice() != null && p.getSellingPrice().compareTo(request.getMaxPrice()) <= 0))
                .filter(p -> !Boolean.TRUE.equals(request.getInStockOnly()) || Boolean.TRUE.equals(p.getInStock()))
                .collect(Collectors.toList());

        int totalElements = filtered.size();
        int fromIndex = Math.min((int) pageable.getOffset(), totalElements);
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalElements);

        List<SearchProductDTO> content = filtered.subList(fromIndex, toIndex).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        int totalPages = pageable.getPageSize() > 0 ? (int) Math.ceil((double) totalElements / pageable.getPageSize()) : 0;

        return PagedResponse.<SearchProductDTO>builder()
                .content(content)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(pageable.getPageNumber() >= totalPages - 1)
                .isFirst(pageable.getPageNumber() == 0)
                .build();
    }

    @Override
    @Cacheable(value = "search", key = "'suggest-' + #prefix")
    public List<SearchSuggestionDTO> getSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.trim().length() < 2) {
            return List.of();
        }

        String searchPrefix = prefix.trim().toLowerCase();
        int max = limit > 0 && limit <= 20 ? limit : 10;

        Iterable<SearchProduct> allProducts = searchProductRepository.findAll();
        return StreamSupport.stream(allProducts.spliterator(), false)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(searchPrefix))
                .limit(max)
                .map(this::mapToSuggestionDTO)
                .collect(Collectors.toList());
    }
}
