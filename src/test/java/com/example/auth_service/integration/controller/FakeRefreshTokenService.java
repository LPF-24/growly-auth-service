package com.example.auth_service.integration.controller;

import com.example.auth_service.security.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class FakeRefreshTokenService extends RefreshTokenService {
    private boolean shouldThrow = false;

    public FakeRefreshTokenService() {
        super(null); // RedisTemplate не нужен, потому что методы переопределяются
    }

    public void setShouldThrow(boolean shouldThrow) {
        this.shouldThrow = shouldThrow;
    }

    @Override
    public void saveRefreshToken(String username, String refreshToken) {
        if (shouldThrow) {
            throw new RuntimeException("Simulated failure");
        }
    }

    @Override
    public String getRefreshToken(String username) {
        return null;
    }

    @Override
    public void deleteRefreshToken(String username) {
        // ничего не делаем
    }
}
