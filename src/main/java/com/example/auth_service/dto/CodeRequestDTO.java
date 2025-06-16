package com.example.auth_service.dto;

import com.example.auth_service.validation.annotation.ValidCode;

public class CodeRequestDTO {
    @ValidCode
    private String code;

    public CodeRequestDTO(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
