package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;

import java.util.UUID;

public interface TokenService {

    RefreshToken createRefreshToken(User user, String ipAddress, String deviceInfo);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void blacklistAccessToken(String accessToken);

    boolean isTokenBlacklisted(String accessToken);
}
