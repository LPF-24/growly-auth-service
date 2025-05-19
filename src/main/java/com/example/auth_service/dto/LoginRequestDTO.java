package com.example.auth_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LoginRequestDTO {
    private String username;

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
