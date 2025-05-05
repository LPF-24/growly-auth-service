package com.example.auth_service.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.auth_service.security.JWTUtil;
import com.example.auth_service.security.PersonDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    private final JWTUtil jwtUtil;
    private final PersonDetailsService personDetailsService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, PersonDetailsService personDetailsService) {
        this.jwtUtil = jwtUtil;
        this.personDetailsService = personDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header = {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = authHeader.substring(7);
        if (jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT decodedJWT = jwtUtil.validateAccessToken(jwt);
            String username = decodedJWT.getClaim("username").asString();
            String role = decodedJWT.getClaim("role").asString();

            if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = personDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            logger.error("Error in JWT filter: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
