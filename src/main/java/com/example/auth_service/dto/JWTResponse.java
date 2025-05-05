package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTResponse {
    private String token;

    private Long id;

    private String username;
}
