package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.exception.ErrorResponseDTO;
import com.example.auth_service.exception.ValidationException;
import com.example.auth_service.security.JWTUtil;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.security.PersonDetailsService;
import com.example.auth_service.security.RefreshTokenService;
import com.example.auth_service.service.LogoutService;
import com.example.auth_service.service.PeopleService;
import com.example.auth_service.validation.validator.PersonValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/")
@Tag(name = "Authentication", description = "Endpoints for login, registration and user actions")
public class AuthController {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PersonDetailsService personDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final PeopleService peopleService;
    private final PersonValidator personValidator;
    private final LogoutService logoutService;

    public AuthController(JWTUtil jwtUtil, AuthenticationManager authenticationManager, PersonDetailsService personDetailsService, RefreshTokenService refreshTokenService, PeopleService peopleService, PersonValidator personValidator, LogoutService logoutService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.personDetailsService = personDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.peopleService = peopleService;
        this.personValidator = personValidator;
        this.logoutService = logoutService;
    }

    @Operation(summary = "Login",
            description = "Method to login using login and password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User data: authentication token, id, username, email.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "User successfully logged in.",
                                            value = "{ \"accessToken\": \"token...\", \"id\": \"20\", \"username\": \"maria123\", \"email\": \"maria123@gmail.com\" }"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{ \"status\": 401, \"message\": \"Invalid username or password\", \"path\": \"/login\" }"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{ \"status\": 500, \"error\": \"Internal Server Error\", \"path\": \"/login\" }"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{ \"timestamp\": \"2025-06-20T11:11:00.038+00:00\", \"path\": \"/login\", \"status\": 503, \"error\": \"Service unavailable\", \"requestId\": \"335c1b74-7\" }"
                                    )))
            })
    @PostMapping("/login")
    public ResponseEntity<?> performAuthentication(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        try {
            logger.debug(">>> Received login request: {}", loginRequest.getUsername());
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
            String role = personDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            String accessToken = jwtUtil.generateAccessToken(personDetails.getId(), personDetails.getUsername(), role);
            String refreshToken = jwtUtil.generateRefreshToken(personDetails.getUsername());

            refreshTokenService.saveRefreshToken(personDetails.getUsername(), refreshToken);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Lax")
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            peopleService.setLastLogin(personDetails.getId());

            return ResponseEntity.ok(new JWTResponse(accessToken, personDetails.getId(), personDetails.getUsername(), personDetails.getEmail()));
        } catch (BadCredentialsException e) {
            ErrorResponseDTO error = new ErrorResponseDTO();
            error.setStatus(401);
            error.setMessage("Invalid username or password");
            error.setPath("/login");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @Operation(summary = "Registration method",
            description = "Method for registering a person in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User data: id, username, email, role.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "User registered successfully.",
                                            value = "{ \"id\": \"21\", \"username\": \"maria12\", \"email\": \"maria12@gmail.com\", \"role\": \"ROLE_USER\" }"
                                    ))),
                    @ApiResponse(responseCode = "400", description = "Bad Request.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Bad Request",
                                            summary = "Example of 400 Bad Request",
                                            value = "{\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"message\": \"username: This username is already taken!; email: This email is already taken!\",\n" +
                                                    "  \"path\": \"/registration\"\n" +
                                                    "}"
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
                                                    "  \"path\": \"/registration\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{ \"timestamp\": \"2025-06-20T11:11:00.038+00:00\", \"path\": \"/registration\", \"status\": 503, \"error\": \"Service unavailable\", \"requestId\": \"335c1b74-7\" }"
                                    )))
            })
    @PostMapping("/registration")
    public ResponseEntity<PersonResponseDTO> register(@RequestBody @Valid PersonRequestDTO dto, BindingResult bindingResult) {
        personValidator.validate(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.error("Binding result has errors: ");
            bindingResult.getFieldErrors().forEach(fieldError ->
                    logger.error(fieldError.getDefaultMessage()));
            throw new ValidationException(bindingResult);
        }

        logger.info("Middle of the method");
        PersonResponseDTO response = peopleService.savePerson(dto);
        logger.info("User {} successfully created", dto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete method",
            description = "Method for user to delete himself.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion message.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "User successfully deleted.",
                                            value = "{ \"message\": \"User's account with id 21 successfully deleted.\" }"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{ \"path\": \"/delete\", \"message\": \"Unauthorized: missing or invalid token\", \"status\": 401 }"
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
                                                    "  \"path\": \"/delete\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{ \"timestamp\": \"2025-06-20T11:11:00.038+00:00\", \"path\": \"/delete\", \"status\": 503, \"error\": \"Service unavailable\", \"requestId\": \"335c1b74-7\" }"
                                    )))
            })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePerson(@AuthenticationPrincipal PersonDetails personDetails) {
        Long personId = personDetails.getId();
        peopleService.deletePerson(personId); // один вызов
        return ResponseEntity.ok("User's account with id " + personId + " successfully deleted.");
    }

    @Operation(summary = "Loading user data after login",
            description = "Method for obtaining user data for personal account.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Personal account details.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "Personal account data successfully received.",
                                            value = "{ \"id\": \"8\", \"username\": \"max\", \"email\": \"max@gmail.com\", \"role\": \"ROLE_ADMIN\" }"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{ \"path\": \"/profile\", \"message\": \"Unauthorized: missing or invalid token\", \"status\": 401 }"
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
                                                    "  \"path\": \"/profile\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{ \"timestamp\": \"2025-06-20T11:11:00.038+00:00\", \"path\": \"/profile\", \"status\": 503, \"error\": \"Service unavailable\", \"requestId\": \"335c1b74-7\" }"
                                    )))
            })
    @GetMapping("/profile")
    public ResponseEntity<PersonResponseDTO> getProfileInfo() {
        return ResponseEntity.ok(peopleService.getCurrentUserInfo());
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "New access token successfully issued"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{ \"path\": \"/refresh\", \"message\": \"Unauthorized: missing or invalid token\", \"status\": 401 }"
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
                                                    "  \"path\": \"/refresh\"\n" +
                                                    "}"
                                    )))
            })
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        try {
            String username = jwtUtil.validateRefreshToken(refreshToken).getClaim("username").asString();
            Long id = ((PersonDetails) personDetailsService.loadUserByUsername(username)).getId();
            String role = personDetailsService.loadUserByUsername(username).getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            String savedToken = refreshTokenService.getRefreshToken(username);

            if (!refreshToken.equals(savedToken)) {
                throw new RuntimeException("Refresh token is invalid or expired");
            }

            String newAccessToken = jwtUtil.generateAccessToken(id, username, role);

            return ResponseEntity.ok(Map.of("access_token", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }
    }

    @Operation(summary = "Logout the user", description = "Invalidates the refresh token and deletes the cookie.",
    responses = {
            @ApiResponse(responseCode = "200", description = "Delete refresh token cookie"),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    summary = "Example of 401 Unauthorized",
                                    value = "{ \"path\": \"/logout\", \"message\": \"Unauthorized: missing or invalid token\", \"status\": 401 }"
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
                                            "  \"path\": \"/logout\"\n" +
                                            "}"
                            )))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshToken) {
        String username = jwtUtil.validateRefreshToken(refreshToken).getClaim("username").asString();
        return logoutService.buildLogoutResponse(username);
    }

    @Operation(summary = "Update method",
            description = "Method of updating user data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User data: authentication token, id, username, email.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "OK",
                                            summary = "user data updated successfully.",
                                            value = "{ \"id\": \"8\", \"username\": \"max\", \"email\": \"max25@gmail.com\", \"role\": \"ROLE_ADMIN\" }"
                                    ))),
                    @ApiResponse(responseCode = "400", description = "Bad Request.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Bad Request",
                                            summary = "Example of 400 Bad Request",
                                            value = "{\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"message\": \"email: Email should be valid\",\n" +
                                                    "  \"path\": \"/update\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{ \"path\": \"/update\", \"message\": \"Unauthorized: missing or invalid token\", \"status\": 401 }"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{ \"status\": 500, \"error\": \"Internal Server Error\", \"path\": \"/update\" }"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{ \"timestamp\": \"2025-06-20T11:11:00.038+00:00\", \"path\": \"/update\", \"status\": 503, \"error\": \"Service unavailable\", \"requestId\": \"335c1b74-7\" }"
                                    )))
            })
    @PatchMapping("/update")
    public ResponseEntity<PersonResponseDTO> updateUserInfo(@RequestBody @Valid PersonUpdateDTO dto, BindingResult bindingResult) {
        personValidator.validate(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.error("Binding result has errors: ");
            bindingResult.getFieldErrors().forEach(fieldError ->
                    logger.error(fieldError.getDefaultMessage()));
            throw new ValidationException(bindingResult);
        }

        logger.debug("Middle of the method");
        PersonResponseDTO response = peopleService.updateCurrentUserInfo(dto);
        return ResponseEntity.ok(response);
    }
}
