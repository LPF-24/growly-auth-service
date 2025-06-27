package com.example.auth_service.controller;

import com.example.auth_service.dto.CodeRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.UserStatsDTO;
import com.example.auth_service.exception.ErrorResponseDTO;
import com.example.auth_service.exception.ValidationException;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.service.AdminService;
import com.example.auth_service.service.LogoutService;
import com.example.auth_service.service.PeopleService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Endpoints for change user role and admin actions")
public class AdminController {
    private final AdminService adminService;
    private final LogoutService logoutService;
    private final PeopleService peopleService;
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    public AdminController(AdminService adminService, LogoutService logoutService, PeopleService peopleService) {
        this.adminService = adminService;
        this.logoutService = logoutService;
        this.peopleService = peopleService;
    }

    @Operation(summary = "Change ROLE_USER to ROLE_ADMIN",
            description = "Method to change user role from ROLE_USER to ROLE_ADMIN followed by logging out of the account.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout message.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "The user has been promoted to admin and logged out.",
                                            value = "{\n  \"message\": \"Logged out successfully\"\n}"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{\n  \"path\": \"/admin/promote\",\n  \"message\": \"Unauthorized: missing or invalid token\",\n  \"status\": 401\n}"
                                    ))),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Forbidden",
                                            summary = "Example of 403 Forbidden",
                                            value = "{\n  \"path\": \"/admin/promote\",\n  \"message\": \"Access denied: insufficient permissions\",\n  \"status\": 403\n}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n  \"status\": 500,\n  \"error\": \"Internal Server Error\",\n  \"path\": \"/admin/promote\"\n}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{\n  \"timestamp\": \"2025-06-20T11:11:00.038+00:00\",\n  \"path\": \"/admin/promote\",\n  \"status\": 503,\n  \"error\": \"Service unavailable\",\n  \"requestId\": \"335c1b74-7\"\n}"
                                    )))
            })
    @PatchMapping("/promote")
    public ResponseEntity<?> promote(@RequestBody @Valid CodeRequestDTO code, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            logger.error("BindingResult has errors: ");
            bindingResult.getFieldErrors().forEach(fieldError -> {
                logger.error(fieldError.getDefaultMessage());
            });
            throw new ValidationException(bindingResult);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        logger.debug("Middle of the method");
        adminService.promotePerson(personDetails.getId());
        logger.info("Promotion was successful");
        return logoutService.buildLogoutResponse(personDetails.getUsername());
    }

    @Operation(summary = "Get all users",
            description = "Method for getting some data of all users by user with ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Some information about all users.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PersonResponseDTO.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{\n  \"path\": \"/admin/all-users\",\n  \"message\": \"Unauthorized: missing or invalid token\",\n  \"status\": 401\n}"
                                    ))),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Forbidden",
                                            summary = "Example of 403 Forbidden",
                                            value = "{\n  \"path\": \"/admin/all-users\",\n  \"message\": \"Access denied: insufficient permissions\",\n  \"status\": 403\n}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/admin/all-users\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{\n  \"timestamp\": \"2025-06-20T11:11:00.038+00:00\",\n  \"path\": \"/admin/all-users\",\n  \"status\": 503,\n  \"error\": \"Service unavailable\",\n  \"requestId\": \"335c1b74-7\"\n}"
                                    )))
            })
    @GetMapping("/all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PersonResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.findAllUsers());
    }

    @Operation(summary = "Get all users statistics",
            description = "Method for obtaining user statistics for a user with the ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Some user statistics.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserStatsDTO.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{\n  \"path\": \"/admin/stats\",\n  \"message\": \"Unauthorized: missing or invalid token\",\n  \"status\": 401\n}"
                                    ))),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Forbidden",
                                            summary = "Example of 403 Forbidden",
                                            value = "{\n  \"path\": \"/admin/stats\",\n  \"message\": \"Access denied: insufficient permissions\",\n  \"status\": 403\n}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/admin/stats\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{\n  \"timestamp\": \"2025-06-20T11:11:00.038+00:00\",\n  \"path\": \"/admin/stats\",\n  \"status\": 503,\n  \"error\": \"Service unavailable\",\n  \"requestId\": \"335c1b74-7\"\n}"
                                    )))
            })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserStatsDTO> getUserStats() {
        return adminService.getAllUserStats();
    }

    @Operation(summary = "Deleting users by admin",
            description = "Method that allows a user with the ADMIN role to delete other users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Some user statistics.",
                            content = @Content(
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "User successfully deleted by admin.",
                                            value = "{\n" +
                                                    " \" User with ID 13 was deleted by admin\", \n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{\n  \"path\": \"/admin/delete/{id}\",\n  \"message\": \"Unauthorized: missing or invalid token\",\n  \"status\": 401\n}"
                                    ))),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Forbidden",
                                            summary = "Example of 403 Forbidden",
                                            value = "{\n  \"path\": \"/admin/delete/{id}\",\n  \"message\": \"Access denied: insufficient permissions\",\n  \"status\": 403\n}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/admin/delete/13\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{\n  \"timestamp\": \"2025-06-20T11:11:00.038+00:00\",\n  \"path\": \"/admin/delete/{id}\",\n  \"status\": 503,\n  \"error\": \"Service unavailable\",\n  \"requestId\": \"335c1b74-7\"\n}"
                                    )))
            })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserByAdmin(@PathVariable Long id) {
        peopleService.deletePerson(id);
        return ResponseEntity.ok("User with ID " + id + " was deleted by admin");
    }
}
