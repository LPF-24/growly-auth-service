package com.example.auth_service.dto;

import com.example.auth_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LoginRequestDTO {
    @Schema(description = SwaggerConstants.USERNAME_DESC, example = SwaggerConstants.USERNAME_EXAMPLE)
    private String username;

    @Schema(description = SwaggerConstants.PASSWORD_DESC, example = SwaggerConstants.PASSWORD_EXAMPLE)
    private String password;

    public LoginRequestDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
