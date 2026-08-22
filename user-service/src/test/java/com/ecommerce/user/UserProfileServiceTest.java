package com.ecommerce.user;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.user.dto.UserProfileDTO;
import com.ecommerce.user.dto.UserProfileUpdateRequest;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.repository.UserProfileRepository;
import com.ecommerce.user.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UUID userId;
    private UserProfile sampleProfile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .firstName("Alex")
                .lastName("Morgan")
                .phone("+1234567890")
                .build();
    }

    @Test
    @DisplayName("Should retrieve profile by userId")
    void testGetProfileByUserId() {
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(sampleProfile));

        UserProfileDTO dto = userProfileService.getProfileByUserId(userId);

        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
        assertEquals("Alex", dto.getFirstName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile not found")
    void testGetProfileNotFound() {
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userProfileService.getProfileByUserId(userId));
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile() {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .firstName("Alexander")
                .lastName("Morgan Jr.")
                .phone("+9876543210")
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(sampleProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleProfile);

        UserProfileDTO updated = userProfileService.updateProfile(userId, request);

        assertNotNull(updated);
        assertEquals("Alexander", sampleProfile.getFirstName());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }
}
