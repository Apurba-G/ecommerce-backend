package com.ecommerce.cart;

import com.ecommerce.cart.dto.CartDTO;
import com.ecommerce.cart.dto.CartItemAddRequest;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void testAddItemToCart_Success() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = Cart.builder()
                .id(cartId)
                .userId(userId)
                .cartStatus("ACTIVE")
                .subtotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserIdAndCartStatus(userId, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductIdAndVariantIdIsNull(cartId, productId)).thenReturn(Optional.empty());
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        CartItemAddRequest req = CartItemAddRequest.builder()
                .productId(productId)
                .productName("iPhone 15 Pro")
                .unitPrice(new BigDecimal("999.00"))
                .sellingPrice(new BigDecimal("899.00"))
                .quantity(2)
                .build();

        CartDTO result = cartService.addItemToCart(userId, null, req);

        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
        assertEquals("iPhone 15 Pro", cart.getItems().get(0).getProductName());
        verify(cartItemRepository, times(1)).save(any());
    }
}
