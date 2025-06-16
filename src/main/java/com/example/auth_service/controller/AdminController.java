package com.example.auth_service.controller;

import com.example.auth_service.dto.CodeRequestDTO;
import com.example.auth_service.exception.ValidationException;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.service.AdminService;
import com.example.auth_service.service.LogoutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AuthenticationManager authenticationManager;
    private final AdminService adminService;
    private final LogoutService logoutService;

    public AdminController(AuthenticationManager authenticationManager, AdminService adminService, LogoutService logoutService) {
        this.authenticationManager = authenticationManager;
        this.adminService = adminService;
        this.logoutService = logoutService;
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public void testMethod() {
        System.out.println("Test successful");
        System.out.println("-------------------------------------------------------------");
    }

    @PatchMapping("/promote")
    public ResponseEntity<?> promote(@RequestBody @Valid CodeRequestDTO code, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            System.out.println("BindingResult has errors: ");
            bindingResult.getFieldErrors().forEach(fieldError -> {
                System.out.println(fieldError.getDefaultMessage());
            });
            throw new ValidationException(bindingResult);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        System.out.println("Middle of the method");
        adminService.promotePerson(personDetails.getId());
        System.out.println("Promotion was successful");
        return logoutService.buildLogoutResponse(personDetails.getUsername());
    }
}
