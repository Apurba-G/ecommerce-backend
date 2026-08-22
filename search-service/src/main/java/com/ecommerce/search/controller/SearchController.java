package com.ecommerce.search.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.search.dto.SearchFilterRequest;
import com.ecommerce.search.dto.SearchProductDTO;
import com.ecommerce.search.dto.SearchSuggestionDTO;
import com.ecommerce.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search & Discovery Engine", description = "High-performance full-text search, multi-faceted filtering, and autocomplete suggestions")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Search products with multi-attribute filtering", description = "Executes full-text keyword search and applies category, brand, price, and stock filters")
    public ResponseEntity<ApiResponse<PagedResponse<SearchProductDTO>>> search(
            @Parameter(description = "Search keyword (e.g. 'iPhone 15', 'laptop')") @RequestParam(value = "q", required = false) String query,
            @Parameter(description = "Category UUID filter") @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @Parameter(description = "Brand UUID filter") @RequestParam(value = "brandId", required = false) UUID brandId,
            @Parameter(description = "Minimum price") @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @Parameter(description = "Filter only in-stock products") @RequestParam(value = "inStockOnly", defaultValue = "false") boolean inStockOnly,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "20") int size,
            @Parameter(description = "Sort field (sellingPrice, rating, createdAt)") @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC, DESC)") @RequestParam(value = "direction", defaultValue = "DESC") String direction
    ) {
        String sortColumn = switch (sortBy) {
            case "sellingPrice" -> "selling_price";
            case "basePrice" -> "base_price";
            case "rating" -> "rating";
            case "name" -> "name";
            default -> "created_at";
        };
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortColumn).ascending() : Sort.by(sortColumn).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        SearchFilterRequest filterRequest = SearchFilterRequest.builder()
                .query(query)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStockOnly(inStockOnly)
                .build();

        PagedResponse<SearchProductDTO> results = searchService.search(filterRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Search results retrieved successfully"));
    }

    @GetMapping("/suggest")
    @Operation(summary = "Get autocomplete suggestions", description = "Returns instant typing suggestions for search bar dropdowns")
    public ResponseEntity<ApiResponse<List<SearchSuggestionDTO>>> getSuggestions(
            @Parameter(description = "Search prefix (minimum 2 chars)", required = true) @RequestParam("prefix") String prefix,
            @Parameter(description = "Max number of suggestions to return") @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        List<SearchSuggestionDTO> suggestions = searchService.getSuggestions(prefix, limit);
        return ResponseEntity.ok(ApiResponse.success(suggestions, "Autocomplete suggestions retrieved successfully"));
    }
}
