package com.example.auth_service.unit.service;

import com.example.auth_service.security.RefreshTokenService;
import com.example.auth_service.service.LogoutService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTests {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LogoutService logoutService;

    @Nested
    class LogoutResponseTests {
        @Test
        void buildLogoutResponse_shouldDeleteRefreshTokenAndReturnProperResponse() {
            String username = "john";

            ResponseEntity<Map<String, String>> response = logoutService.buildLogoutResponse(username);

            verify(refreshTokenService).deleteRefreshToken(username);

            Map<String, String> responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals("Logged out successfully", responseBody.get("message"));

            String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
            assertNotNull(setCookieHeader);
            assertTrue(setCookieHeader.contains("refreshToken="));
            assertTrue(setCookieHeader.contains("Max-Age=0"));
            assertTrue(setCookieHeader.contains("HttpOnly"));
            assertTrue(setCookieHeader.contains("Secure"));
        }
    }
}
