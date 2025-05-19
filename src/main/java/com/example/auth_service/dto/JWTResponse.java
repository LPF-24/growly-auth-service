package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


public class JWTResponse {
    private String accessToken;

    private Long id;

    private String username;

    public JWTResponse(String accessToken, Long id, String username) {
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
    }

    public JWTResponse() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
