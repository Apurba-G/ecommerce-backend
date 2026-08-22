package com.ecommerce.auth;

import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.RegisterRequest;
import com.ecommerce.auth.dto.response.AuthResponse;
import com.ecommerce.auth.dto.response.UserResponse;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.event.UserEventPublisher;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.TokenService;
import com.ecommerce.auth.service.impl.AuthServiceImpl;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_CUSTOMER")
                .permissions(new HashSet<>())
                .build();

        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("alex@example.com")
                .username("alexmorgan")
                .password("encodedPassword")
                .firstName("Alex")
                .lastName("Morgan")
                .accountStatus("ACTIVE")
                .roles(Set.of(customerRole))
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alex@example.com")
                .username("alexmorgan")
                .password("Password@123")
                .firstName("Alex")
                .lastName("Morgan")
                .build();

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alexmorgan")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("alex@example.com", response.getEmail());
        verify(userEventPublisher, times(1)).publishUserRegistered(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw BusinessException when email is duplicate")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alex@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .emailOrUsername("alex@example.com")
                .password("Password@123")
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(sampleUser)
                .token("mock-refresh-token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("mock-access-token");
        when(tokenService.createRefreshToken(any(), any(), any())).thenReturn(refreshToken);

        AuthResponse response = authService.login(request, "127.0.0.1", "JUnit-Agent");

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException on wrong password")
    void testLoginWrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .emailOrUsername("alex@example.com")
                .password("WrongPassword")
                .build();

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request, "127.0.0.1", "JUnit-Agent"));
    }
}
