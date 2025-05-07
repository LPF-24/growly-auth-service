package com.example.auth_service.controller;

import com.example.auth_service.dto.JWTResponse;
import com.example.auth_service.dto.LoginRequestDTO;
import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.security.JWTUtil;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.security.PersonDetailsService;
import com.example.auth_service.security.RefreshTokenService;
import com.example.auth_service.service.PeopleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthController {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PersonDetailsService personDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final PeopleService peopleService;

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

            String accessToken = jwtUtil.generateAccessToken(personDetails.getUsername(), role);
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

            return ResponseEntity.ok(new JWTResponse(accessToken, personDetails.getId(), personDetails.getUsername()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JWTResponse(null, null, "Invalid username or password"));
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<PersonResponseDTO> register(@RequestBody @Valid PersonRequestDTO dto) {
        PersonResponseDTO response = peopleService.savePerson(dto);
        logger.info("User {} successfully created", dto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePerson(@PathVariable("id") Long personId) {
        peopleService.deletePerson(personId);
        return ResponseEntity.ok("User's account with id " + personId + " successfully deleted.");
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PersonResponseDTO> getProfileInfo() {
        return ResponseEntity.ok(peopleService.getCurrentUserInfo());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        try {
            String username = jwtUtil.validateRefreshToken(refreshToken).getClaim("username").asString();
            String role = personDetailsService.loadUserByUsername(username).getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            String savedToken = refreshTokenService.getRefreshToken(username);

            if (!refreshToken.equals(savedToken)) {
                throw new RuntimeException("Refresh token is invalid or expired");
            }

            String newAccessToken = jwtUtil.generateAccessToken(username, role);

            return ResponseEntity.ok(Map.of("access_token", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }
    }
}
