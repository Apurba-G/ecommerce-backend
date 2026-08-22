package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.SavedCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedCartRepository extends JpaRepository<SavedCart, UUID> {
    List<SavedCart> findByUserId(UUID userId);
}
