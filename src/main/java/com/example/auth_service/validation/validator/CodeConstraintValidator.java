package com.example.auth_service.validation.validator;

import com.example.auth_service.validation.annotation.ValidCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CodeConstraintValidator implements ConstraintValidator<ValidCode, String> {
    @Override
    public boolean isValid(String code, ConstraintValidatorContext context) {
        if (code == null || code.isBlank()) {
            return false;
        }

        try {
            int value = Integer.parseInt(code);
            return value >= 100_000 && value <= 999_999 && value % 4 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
