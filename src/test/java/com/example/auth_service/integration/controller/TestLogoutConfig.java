package com.example.auth_service.integration.controller;

import com.example.auth_service.security.RefreshTokenService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestLogoutConfig {

    @Bean
    @Primary
    public RefreshTokenService refreshTokenService() {
        return new FakeRefreshTokenService();
    }
}

