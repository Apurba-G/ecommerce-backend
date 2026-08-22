package com.ecommerce.search.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.search.dto.SearchFilterRequest;
import com.ecommerce.search.dto.SearchProductDTO;
import com.ecommerce.search.dto.SearchSuggestionDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchService {
    PagedResponse<SearchProductDTO> search(SearchFilterRequest request, Pageable pageable);
    List<SearchSuggestionDTO> getSuggestions(String prefix, int limit);
}
