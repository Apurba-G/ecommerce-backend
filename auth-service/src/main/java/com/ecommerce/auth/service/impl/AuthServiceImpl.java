package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.RefreshTokenRequest;
import com.ecommerce.auth.dto.request.RegisterRequest;
import com.ecommerce.auth.dto.response.AuthResponse;
import com.ecommerce.auth.dto.response.UserResponse;
import com.ecommerce.auth.entity.Permission;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.event.UserEventPublisher;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.service.TokenService;
import com.ecommerce.common.constant.CommonErrorCode;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE, "Email is already registered");
        }

        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE, "Username is already taken");
        }

        String targetRoleName = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim().toUpperCase()
                : "ROLE_CUSTOMER";

        if (!targetRoleName.startsWith("ROLE_")) {
            targetRoleName = "ROLE_" + targetRoleName;
        }

        final String finalRoleName = targetRoleName;
        Role userRole = roleRepository.findByName(finalRoleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(finalRoleName).description(finalRoleName).build()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .username(request.getUsername() != null ? request.getUsername().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .accountStatus("ACTIVE")
                .provider("LOCAL")
                .emailVerified(false)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Publish event to RabbitMQ for user-service to create profile
        userEventPublisher.publishUserRegistered(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhone()
        );

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String deviceInfo) {
        String identifier = request.getEmailOrUsername().trim().toLowerCase();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UnauthorizedException(CommonErrorCode.INVALID_CREDENTIALS, "Invalid email/username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(CommonErrorCode.INVALID_CREDENTIALS, "Invalid email/username or password");
        }

        if (!"ACTIVE".equals(user.getAccountStatus())) {
            throw new UnauthorizedException("Account is " + user.getAccountStatus().toLowerCase());
        }

        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), user.getUsername(), roleNames, permissions);
        RefreshToken refreshToken = tokenService.createRefreshToken(user, ipAddress, deviceInfo);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roleNames)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = tokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), user.getUsername(), roleNames, permissions);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roleNames)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken, String authHeader) {
        if (refreshToken != null) {
            tokenService.revokeRefreshToken(refreshToken);
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.blacklistAccessToken(token);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .accountStatus(user.getAccountStatus())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();
    }
}
