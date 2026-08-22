package com.ecommerce.wishlist;

import com.ecommerce.wishlist.dto.WishlistCreateRequest;
import com.ecommerce.wishlist.dto.WishlistDTO;
import com.ecommerce.wishlist.dto.WishlistItemAddRequest;
import com.ecommerce.wishlist.entity.Wishlist;
import com.ecommerce.wishlist.repository.WishlistItemRepository;
import com.ecommerce.wishlist.repository.WishlistRepository;
import com.ecommerce.wishlist.service.impl.WishlistServiceImpl;
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
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @Test
    void testCreateWishlist_Success() {
        UUID userId = UUID.randomUUID();
        WishlistCreateRequest req = WishlistCreateRequest.builder()
                .name("Holiday Tech")
                .isDefault(false)
                .isPublic(true)
                .build();

        Wishlist saved = Wishlist.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name(req.getName())
                .isDefault(false)
                .isPublic(true)
                .shareToken("wishlist-abcd1234")
                .items(new ArrayList<>())
                .build();

        when(wishlistRepository.existsByUserIdAndName(userId, "Holiday Tech")).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(saved);

        WishlistDTO result = wishlistService.createWishlist(userId, req);

        assertNotNull(result);
        assertEquals("Holiday Tech", result.getName());
        assertEquals("wishlist-abcd1234", result.getShareToken());
    }

    @Test
    void testAddItemToWishlist_Success() {
        UUID userId = UUID.randomUUID();
        UUID wishlistId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Wishlist wishlist = Wishlist.builder()
                .id(wishlistId)
                .userId(userId)
                .name("Favorites")
                .items(new ArrayList<>())
                .build();

        when(wishlistRepository.findById(wishlistId)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findByWishlistIdAndProductIdAndVariantIdIsNull(wishlistId, productId)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        WishlistItemAddRequest req = WishlistItemAddRequest.builder()
                .productId(productId)
                .productName("Wireless Headphones")
                .price(new BigDecimal("199.99"))
                .inStock(true)
                .build();

        WishlistDTO result = wishlistService.addItemToWishlist(userId, wishlistId, req);

        assertNotNull(result);
        assertEquals(1, wishlist.getItems().size());
        assertEquals("Wireless Headphones", wishlist.getItems().get(0).getProductName());
    }
}
