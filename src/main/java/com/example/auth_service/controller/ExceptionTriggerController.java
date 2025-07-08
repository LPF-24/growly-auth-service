package com.example.auth_service.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.BadRequestException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-errors")
public class ExceptionTriggerController {

    @DeleteMapping("/mismatch/{id}")
    public void triggerTypeMismatch(@PathVariable Long id) {
        // ничего не делать — типизация уже приведет к ошибке
    }

    @GetMapping("/entity-not-found")
    public void triggerEntityNotFound() {
        throw new EntityNotFoundException("User not found");
    }

    @GetMapping("/constraint/{id}")
    public void triggerConstraintViolation(@PathVariable @Min(5) Long id) {
    }

    @GetMapping("/bad-request")
    public void triggerBadRequestException() {
        throw new BadRequestException("Something is wrong");
    }

    @GetMapping("/runtime-exception")
    public void triggeredRuntimeException() {
        throw new RuntimeException("Unexpected error");
    }
}

