package com.ecommerce.category;

import com.ecommerce.category.dto.CategoryCreateRequest;
import com.ecommerce.category.dto.CategoryDTO;
import com.ecommerce.category.entity.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.category.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        UUID rootId = UUID.randomUUID();
        rootCategory = Category.builder()
                .id(rootId)
                .name("Electronics")
                .slug("electronics")
                .level(0)
                .path("electronics")
                .isActive(true)
                .build();

        childCategory = Category.builder()
                .id(UUID.randomUUID())
                .parentId(rootId)
                .name("Laptops")
                .slug("laptops")
                .level(1)
                .path("electronics/laptops")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create root category and calculate slug and path")
    void testCreateRootCategory() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Electronics")
                .description("All electronics")
                .build();

        when(categoryRepository.existsBySlug("electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(rootCategory);

        CategoryDTO result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertEquals("electronics", result.getSlug());
    }

    @Test
    @DisplayName("Should build nested category tree")
    void testGetCategoryTree() {
        when(categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(List.of(rootCategory));
        when(categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(rootCategory.getId()))
                .thenReturn(List.of(childCategory));

        List<CategoryDTO> tree = categoryService.getCategoryTree();

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("Electronics", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("Laptops", tree.get(0).getChildren().get(0).getName());
    }

    @Test
    @DisplayName("Should retrieve category by slug")
    void testGetCategoryBySlug() {
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(rootCategory));

        CategoryDTO result = categoryService.getCategoryBySlug("electronics");

        assertNotNull(result);
        assertEquals("electronics", result.getSlug());
    }
}
