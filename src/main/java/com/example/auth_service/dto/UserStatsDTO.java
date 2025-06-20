package com.example.auth_service.dto;

import com.example.auth_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class UserStatsDTO {
    @Schema(description = SwaggerConstants.ID_DESC, example = SwaggerConstants.ID_EXAMPLE)
    private Long id;
    @Schema(description = SwaggerConstants.USERNAME_DESC, example = SwaggerConstants.USERNAME_EXAMPLE)
    private String username;
    @Schema(description = SwaggerConstants.EMAIL_DESC, example = SwaggerConstants.EMAIL_EXAMPLE)
    private String email;
    @Schema(description = SwaggerConstants.ROLE_DESC, example = SwaggerConstants.ROLE_EXAMPLE)
    private String role;
    @Schema(description = SwaggerConstants.LAST_LOGIN_DESC, example = SwaggerConstants.LAST_LOGIN_EXAMPLE)
    private LocalDateTime lastLogin;

    public UserStatsDTO() {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
