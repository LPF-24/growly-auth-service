package com.example.auth_service.dto;

import com.example.auth_service.util.SwaggerConstants;
import com.example.auth_service.validation.annotation.ValidCode;
import io.swagger.v3.oas.annotations.media.Schema;

public class CodeRequestDTO {
    @Schema(description = SwaggerConstants.CODE_DESC, example = SwaggerConstants.CODE_EXAMPLE)
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
