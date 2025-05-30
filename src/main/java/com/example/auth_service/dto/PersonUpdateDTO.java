package com.example.auth_service.dto;

import com.example.auth_service.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class PersonUpdateDTO {
    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters long")
    private String username;

    @ValidPassword
    private String password;

    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email can contain a maximum of 50 characters")
    private String email;

    public PersonUpdateDTO() {
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
