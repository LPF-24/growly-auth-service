package com.example.auth_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PersonResponseDTO {
    private Long id;

    private String username;

    private String email;

    private String role;
}
