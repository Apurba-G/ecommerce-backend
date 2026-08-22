package com.ecommerce.search.repository;

import com.ecommerce.search.entity.SearchProduct;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchProductRepository extends CrudRepository<SearchProduct, String> {

    Optional<SearchProduct> findByProductId(UUID productId);

    List<SearchProduct> findByNameContainingIgnoreCase(String name);

    List<SearchProduct> findByCategoryId(UUID categoryId);

    List<SearchProduct> findByBrandId(UUID brandId);
}
