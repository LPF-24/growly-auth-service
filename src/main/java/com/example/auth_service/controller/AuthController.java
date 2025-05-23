package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.exception.ErrorResponseDTO;
import com.example.auth_service.exception.ValidationException;
import com.example.auth_service.security.JWTUtil;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.security.PersonDetailsService;
import com.example.auth_service.security.RefreshTokenService;
import com.example.auth_service.service.PeopleService;
import com.example.auth_service.validation.validator.PersonValidator;
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
public class AuthController {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PersonDetailsService personDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final PeopleService peopleService;
    private final PersonValidator personValidator;

    public AuthController(JWTUtil jwtUtil, AuthenticationManager authenticationManager, PersonDetailsService personDetailsService, RefreshTokenService refreshTokenService, PeopleService peopleService, PersonValidator personValidator) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.personDetailsService = personDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.peopleService = peopleService;
        this.personValidator = personValidator;
    }

    @PostMapping("/login")
    public ResponseEntity<?> performAuthentication(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        try {
            System.out.println(">>> Received login request: " + loginRequest.getUsername());
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

            return ResponseEntity.ok(new JWTResponse(accessToken, personDetails.getId(), personDetails.getUsername(), personDetails.getEmail()));
        } catch (BadCredentialsException e) {
            ErrorResponseDTO error = new ErrorResponseDTO();
            error.setStatus(401);
            error.setMessage("Invalid username or password");
            error.setPath("/login");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<PersonResponseDTO> register(@RequestBody @Valid PersonRequestDTO dto, BindingResult bindingResult) {
        personValidator.validate(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors: ");
            bindingResult.getFieldErrors().forEach(fieldError ->
                    System.out.println(fieldError.getDefaultMessage()));
            throw new ValidationException(bindingResult);
        }

        logger.info("Middle of the method");
        PersonResponseDTO response = peopleService.savePerson(dto);
        logger.info("User {} successfully created", dto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePerson(@AuthenticationPrincipal PersonDetails personDetails) {
        peopleService.deletePerson(personDetails.getId());
        Long personId = personDetails.getId();

        peopleService.deletePerson(personId);
        return ResponseEntity.ok("User's account with id " + personId + " successfully deleted.");
    }

    @GetMapping("/profile")
    public ResponseEntity<PersonResponseDTO> getProfileInfo() {
        return ResponseEntity.ok(peopleService.getCurrentUserInfo());
    }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshToken) {
        String username = jwtUtil.validateRefreshToken(refreshToken).getClaim("username").asString();
        refreshTokenService.deleteRefreshToken(username);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @PatchMapping
    public ResponseEntity<PersonResponseDTO> updateUserInfo(@RequestBody @Valid PersonUpdateDTO dto, BindingResult bindingResult) {
        personValidator.validate(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors: ");
            bindingResult.getFieldErrors().forEach(fieldError ->
                    System.out.println(fieldError.getDefaultMessage()));
            throw new ValidationException(bindingResult);
        }

        System.out.println("Middle of the method");
        PersonResponseDTO response = peopleService.updateCurrentUserInfo(dto);
        return ResponseEntity.ok(response);
    }
}
