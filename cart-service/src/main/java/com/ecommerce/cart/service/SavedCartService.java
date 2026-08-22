package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.SavedCartCreateRequest;
import com.ecommerce.cart.dto.SavedCartDTO;

import java.util.List;
import java.util.UUID;

public interface SavedCartService {
    SavedCartDTO saveCurrentCart(UUID userId, SavedCartCreateRequest request);
    List<SavedCartDTO> getSavedCarts(UUID userId);
    void deleteSavedCart(UUID userId, UUID savedCartId);
}
