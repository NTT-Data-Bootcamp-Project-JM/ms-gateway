package com.nttdata.bank.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Rejects calls that do not carry a valid JWT, except login, registration, health, and Swagger.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtGatewayFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    /**
     * Builds the HMAC key from the shared bootcamp secret.
     *
     * @param secret configured JWT secret
     */
    public JwtGatewayFilter(@Value("${bank.security.jwt-secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the bearer token before the gateway proxies the request.
     *
     * @param request current request
     * @param response current response
     * @param filterChain remaining filters
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            unauthorized(response, "A bearer token is required");
            return;
        }
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(header.substring(7));
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            unauthorized(response, "The token is invalid or expired");
        }
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/openapi.yaml")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
