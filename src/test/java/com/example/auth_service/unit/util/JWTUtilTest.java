package com.example.auth_service.unit.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.auth_service.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JWTUtilTest {

    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        // вручную установить секрет
        // утилитный класс из org.springframework.test.util, предназначенный для работы с приватными полями и методами в тестах.
        //
        //Он позволяет:
        //1. установить значение приватного поля, даже если у него нет setter,
        //2. вызвать приватный метод.
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret");
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        String token = jwtUtil.generateAccessToken(1L, "john", "ROLE_USER");

        DecodedJWT decodedJWT = jwtUtil.validateAccessToken(token);

        assertEquals("User details", decodedJWT.getSubject());
        assertEquals("john", decodedJWT.getClaim("username").asString());
        assertEquals(1L, decodedJWT.getClaim("id").asLong());
        assertEquals("ROLE_USER", decodedJWT.getClaim("role").asString());
        assertEquals("ADMIN", decodedJWT.getIssuer());
    }

    @Test
    void shouldGenerateAndValidateRefreshToken() {
        String token = jwtUtil.generateRefreshToken("john");

        DecodedJWT decodedJWT = jwtUtil.validateRefreshToken(token);

        assertEquals("RefreshToken", decodedJWT.getSubject());
        assertEquals("john", decodedJWT.getClaim("username").asString());
        assertEquals("ADMIN", decodedJWT.getIssuer());
    }

    @Test
    void shouldThrowException_whenTokenInvalid() {
        assertThrows(Exception.class, () -> jwtUtil.validateAccessToken("invalid.token.value"));
    }
}
