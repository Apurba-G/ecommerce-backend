package com.ecommerce.cart.service.impl;

import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.cart.dto.CartDTO;
import com.ecommerce.cart.dto.CartItemAddRequest;
import com.ecommerce.cart.dto.CartItemDTO;
import com.ecommerce.cart.dto.CartItemUpdateRequest;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private CartItemDTO mapItemToDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .cartId(item.getCart().getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .unitPrice(item.getUnitPrice())
                .sellingPrice(item.getSellingPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .productSnapshot(item.getProductSnapshot())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems() != null
                ? cart.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList())
                : List.of();

        int totalItems = items.stream().mapToInt(CartItemDTO::getQuantity).sum();

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .cartStatus(cart.getCartStatus())
                .couponId(cart.getCouponId())
                .couponCode(cart.getCouponCode())
                .subtotal(cart.getSubtotal())
                .discountAmount(cart.getDiscountAmount())
                .taxAmount(cart.getTaxAmount())
                .shippingAmount(cart.getShippingAmount())
                .totalAmount(cart.getTotalAmount())
                .totalItems(totalItems)
                .items(items)
                .expiresAt(cart.getExpiresAt())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private Cart getOrCreateActiveCart(UUID userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserIdAndCartStatus(userId, "ACTIVE")
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder()
                                .userId(userId)
                                .cartStatus("ACTIVE")
                                .subtotal(BigDecimal.ZERO)
                                .discountAmount(BigDecimal.ZERO)
                                .taxAmount(BigDecimal.ZERO)
                                .shippingAmount(new BigDecimal("5.00"))
                                .totalAmount(new BigDecimal("5.00"))
                                .expiresAt(LocalDateTime.now().plusDays(14))
                                .build();
                        return cartRepository.save(newCart);
                    });
        } else if (sessionId != null && !sessionId.isBlank()) {
            return cartRepository.findBySessionIdAndCartStatus(sessionId, "ACTIVE")
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder()
                                .sessionId(sessionId)
                                .cartStatus("ACTIVE")
                                .subtotal(BigDecimal.ZERO)
                                .discountAmount(BigDecimal.ZERO)
                                .taxAmount(BigDecimal.ZERO)
                                .shippingAmount(BigDecimal.ZERO)
                                .totalAmount(BigDecimal.ZERO)
                                .expiresAt(LocalDateTime.now().plusDays(3))
                                .build();
                        return cartRepository.save(newCart);
                    });
        } else {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Either user authentication or a guest session ID is required");
        }
    }

    @Override
    @Transactional
    @Cacheable(value = "cart", key = "(#userId != null ? #userId.toString() : #sessionId)")
    public CartDTO getCart(UUID userId, String sessionId) {
        Cart cart = getOrCreateActiveCart(userId, sessionId);
        return mapToDTO(cart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "(#userId != null ? #userId.toString() : #sessionId)")
    public CartDTO addItemToCart(UUID userId, String sessionId, CartItemAddRequest request) {
        Cart cart = getOrCreateActiveCart(userId, sessionId);

        Optional<CartItem> existingOpt = request.getVariantId() != null
                ? cartItemRepository.findByCartIdAndProductIdAndVariantId(cart.getId(), request.getProductId(), request.getVariantId())
                : cartItemRepository.findByCartIdAndProductIdAndVariantIdIsNull(cart.getId(), request.getProductId());

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing.setSellingPrice(request.getSellingPrice());
            existing.setUnitPrice(request.getUnitPrice());
            existing.setTotalPrice(request.getSellingPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
            cartItemRepository.save(existing);
        } else {
            BigDecimal total = request.getSellingPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .variantId(request.getVariantId())
                    .productName(request.getProductName())
                    .productImage(request.getProductImage())
                    .unitPrice(request.getUnitPrice())
                    .sellingPrice(request.getSellingPrice())
                    .quantity(request.getQuantity())
                    .totalPrice(total)
                    .productSnapshot(request.getProductSnapshot())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        // Trigger updates cart subtotal & total automatically
        Cart refreshed = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToDTO(refreshed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "(#userId != null ? #userId.toString() : #sessionId)")
    public CartDTO updateItemQuantity(UUID userId, String sessionId, UUID itemId, CartItemUpdateRequest request) {
        Cart cart = getOrCreateActiveCart(userId, sessionId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Item does not belong to your active cart");
        }

        item.setQuantity(request.getQuantity());
        item.setTotalPrice(item.getSellingPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        cartItemRepository.save(item);

        Cart refreshed = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToDTO(refreshed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "(#userId != null ? #userId.toString() : #sessionId)")
    public CartDTO removeItem(UUID userId, String sessionId, UUID itemId) {
        Cart cart = getOrCreateActiveCart(userId, sessionId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Item does not belong to your active cart");
        }

        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartItemRepository.delete(item);

        Cart refreshed = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToDTO(refreshed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "(#userId != null ? #userId.toString() : #sessionId)")
    public CartDTO clearCart(UUID userId, String sessionId) {
        Cart cart = getOrCreateActiveCart(userId, sessionId);
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setTotalAmount(cart.getShippingAmount());
        Cart saved = cartRepository.save(cart);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", allEntries = true)
    public CartDTO mergeGuestCart(UUID userId, String guestSessionId) {
        Optional<Cart> guestCartOpt = cartRepository.findBySessionIdAndCartStatus(guestSessionId, "ACTIVE");
        Cart userCart = getOrCreateActiveCart(userId, null);

        if (guestCartOpt.isEmpty()) {
            return mapToDTO(userCart);
        }

        Cart guestCart = guestCartOpt.get();
        for (CartItem guestItem : guestCart.getItems()) {
            addItemToCart(userId, null, CartItemAddRequest.builder()
                    .productId(guestItem.getProductId())
                    .variantId(guestItem.getVariantId())
                    .productName(guestItem.getProductName())
                    .productImage(guestItem.getProductImage())
                    .unitPrice(guestItem.getUnitPrice())
                    .sellingPrice(guestItem.getSellingPrice())
                    .quantity(guestItem.getQuantity())
                    .productSnapshot(guestItem.getProductSnapshot())
                    .build());
        }

        guestCart.setCartStatus("CONVERTED");
        cartRepository.save(guestCart);
        log.info("Merged guest cart {} into user cart {}", guestSessionId, userCart.getId());

        Cart refreshed = cartRepository.findById(userCart.getId()).orElse(userCart);
        return mapToDTO(refreshed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "(#userId != null ? #userId.toString() : #sessionId)")
    public CartDTO applyCoupon(UUID userId, String sessionId, String couponCode) {
        Cart cart = getOrCreateActiveCart(userId, sessionId);

        if ("DISCOUNT10".equalsIgnoreCase(couponCode)) {
            BigDecimal discount = cart.getSubtotal().multiply(new BigDecimal("0.10"));
            cart.setCouponCode(couponCode.toUpperCase());
            cart.setDiscountAmount(discount);
            cart.setTotalAmount(cart.getSubtotal().subtract(discount).add(cart.getTaxAmount()).add(cart.getShippingAmount()));
        } else {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "Invalid or expired coupon code: " + couponCode);
        }

        Cart saved = cartRepository.save(cart);
        return mapToDTO(saved);
    }
}
