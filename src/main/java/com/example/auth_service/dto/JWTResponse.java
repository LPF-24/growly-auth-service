package com.example.auth_service.dto;

import com.example.auth_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;


public class JWTResponse {
    @Schema(description = SwaggerConstants.ACCESS_TOKEN_DESC, example = SwaggerConstants.ACCESS_TOKEN_EXAMPLE)
    private String accessToken;

    @Schema(description = SwaggerConstants.ID_DESC, example = SwaggerConstants.ID_EXAMPLE)
    private Long id;

    @Schema(description = SwaggerConstants.USERNAME_DESC, example = SwaggerConstants.USERNAME_EXAMPLE)
    private String username;

    @Schema(description = SwaggerConstants.EMAIL_DESC, example = SwaggerConstants.EMAIL_EXAMPLE)
    private String email;

    public JWTResponse(String accessToken, Long id, String username, String email) {
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
