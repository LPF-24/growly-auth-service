package com.example.auth_service;

import com.example.auth_service.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTests {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    // интерфейс из Spring Data Redis, позволяющий работать с простыми значениями в Redis
    // (то есть не списками, множествами и т.д.).
    private ValueOperations<String, String> valueOperations;

    // есть ручная инициализация - @InjectMocks не нужен
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(redisTemplate);
    }

    @Test
    void shouldSaveRefreshToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        refreshTokenService.saveRefreshToken("john", "token123");

        // eq(...) — метод из Mockito, нужен, чтобы указать: "ожидается такой-то точный аргумент":
        // anyLong() — ожидаем любой long (например, 10 или 60)
        //eq(TimeUnit.MINUTES) — ожидаем именно TimeUnit.MINUTES
        verify(valueOperations).set(eq("john"), eq("token123"), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    void shouldGetRefreshToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("john")).thenReturn("token123");

        String result = refreshTokenService.getRefreshToken("john");

        assertEquals("token123", result);
    }

    @Test
    void shouldDeleteRefreshToken() {
        refreshTokenService.deleteRefreshToken("john");

        verify(redisTemplate).delete("john");
    }
}
