package com.example.auth_service.integration.controller;

import com.example.auth_service.security.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class FakeRefreshTokenService extends RefreshTokenService {

    public FakeRefreshTokenService() {
        super(null); // RedisTemplate не нужен, потому что методы переопределяются
    }

    @Override
    public void saveRefreshToken(String username, String refreshToken) {
        // ничего не делаем
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
