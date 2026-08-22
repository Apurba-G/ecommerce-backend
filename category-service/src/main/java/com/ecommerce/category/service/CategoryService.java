package com.ecommerce.category.service;

import com.ecommerce.category.dto.CategoryCreateRequest;
import com.ecommerce.category.dto.CategoryDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryDTO createCategory(CategoryCreateRequest request);

    CategoryDTO getCategoryById(UUID id);

    CategoryDTO getCategoryBySlug(String slug);

    List<CategoryDTO> getCategoryTree();

    List<CategoryDTO> getFeaturedCategories();

    void deleteCategory(UUID id);
}
