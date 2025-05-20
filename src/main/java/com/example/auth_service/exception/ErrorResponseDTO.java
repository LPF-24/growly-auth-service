package com.example.auth_service.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ErrorResponseDTO {

    private int status;

    private String message;

    private String path;

    public ErrorResponseDTO() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
