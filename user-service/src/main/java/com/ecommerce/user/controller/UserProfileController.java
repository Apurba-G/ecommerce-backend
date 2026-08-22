package com.ecommerce.user.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.user.dto.UserProfileDTO;
import com.ecommerce.user.dto.UserProfileUpdateRequest;
import com.ecommerce.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> getMyProfile(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = UUID.fromString(userIdHeader.replace("\"", "").trim());
        UserProfileDTO profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateMyProfile(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UUID userId = UUID.fromString(userIdHeader.replace("\"", "").trim());
        UserProfileDTO updated = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "User profile updated successfully"));
    }
}
