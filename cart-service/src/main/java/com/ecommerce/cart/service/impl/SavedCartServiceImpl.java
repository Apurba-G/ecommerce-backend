package com.ecommerce.cart.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.cart.dto.CartDTO;
import com.ecommerce.cart.dto.SavedCartCreateRequest;
import com.ecommerce.cart.dto.SavedCartDTO;
import com.ecommerce.cart.entity.SavedCart;
import com.ecommerce.cart.repository.SavedCartRepository;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.cart.service.SavedCartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedCartServiceImpl implements SavedCartService {

    private final SavedCartRepository savedCartRepository;
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    private SavedCartDTO mapToDTO(SavedCart s) {
        return SavedCartDTO.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .name(s.getName())
                .cartData(s.getCartData())
                .createdAt(s.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public SavedCartDTO saveCurrentCart(UUID userId, SavedCartCreateRequest request) {
        CartDTO currentCart = cartService.getCart(userId, null);
        String cartJson;
        try {
            cartJson = objectMapper.writeValueAsString(currentCart);
        } catch (Exception e) {
            cartJson = "{}";
        }

        SavedCart savedCart = SavedCart.builder()
                .userId(userId)
                .name(request.getName().trim())
                .cartData(cartJson)
                .build();

        SavedCart saved = savedCartRepository.save(savedCart);
        log.info("Saved current cart for user {} as '{}'", userId, saved.getName());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedCartDTO> getSavedCarts(UUID userId) {
        return savedCartRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSavedCart(UUID userId, UUID savedCartId) {
        SavedCart savedCart = savedCartRepository.findById(savedCartId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedCart", "id", savedCartId));

        if (!savedCart.getUserId().equals(userId)) {
            throw new UnauthorizedException("You cannot delete another user's saved cart");
        }

        savedCartRepository.delete(savedCart);
        log.info("Deleted saved cart id {}", savedCartId);
    }
}
