package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserProfileDTO;
import com.ecommerce.user.dto.UserProfileUpdateRequest;

import java.util.UUID;

public interface UserProfileService {

    UserProfileDTO getProfileByUserId(UUID userId);

    UserProfileDTO updateProfile(UUID userId, UserProfileUpdateRequest request);
}
