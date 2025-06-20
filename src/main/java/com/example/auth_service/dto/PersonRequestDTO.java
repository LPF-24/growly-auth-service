package com.example.auth_service.dto;

import com.example.auth_service.util.SwaggerConstants;
import com.example.auth_service.validation.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PersonRequestDTO {
    @Schema(description = SwaggerConstants.USERNAME_DESC, example = SwaggerConstants.USERNAME_EXAMPLE)
    @NotEmpty(message = "Username can't be empty")
    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters long")
    private String username;

    @Schema(description = SwaggerConstants.PASSWORD_DESC, example = SwaggerConstants.PASSWORD_EXAMPLE)
    @NotEmpty(message = "Password can't be empty")
    @ValidPassword
    private String password;

    @Schema(description = SwaggerConstants.EMAIL_DESC, example = SwaggerConstants.EMAIL_EXAMPLE)
    @NotEmpty(message = "Email can't be empty")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email can contain a maximum of 50 characters")
    private String email;

    public PersonRequestDTO() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
