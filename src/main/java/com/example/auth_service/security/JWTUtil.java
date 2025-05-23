package com.example.auth_service.security;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {
    @Value("${jwt_secret}")
    private String secret;

    public String generateAccessToken(Long id, String username, String role) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                .withSubject("User details")
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withIssuer("ADMIN")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateAccessToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("ADMIN")
                .build();
        return verifier.verify(token);
    }

    public String generateRefreshToken(String username) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusDays(7).toInstant());

        return JWT.create()
                .withSubject("RefreshToken")
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withIssuer("ADMIN")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateRefreshToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .withSubject("RefreshToken")
                .withIssuer("ADMIN")
                .build()
                .verify(token);
    }
}
