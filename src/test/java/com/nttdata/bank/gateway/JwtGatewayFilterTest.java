package com.nttdata.bank.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Gateway JWT filter: public paths stay open and every other path needs a bearer token.
 */
class JwtGatewayFilterTest {

    private static final String SECRET = "bank-bootcamp-demo-secret-change-me";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .addFilters(new JwtGatewayFilter(SECRET))
                .build();
    }

    @Test
    void authLoginDoesNotNeedAToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string("passed"));
    }

    @Test
    void contractAndOptionsStayPublic() throws Exception {
        mockMvc.perform(get("/openapi.yaml")).andExpect(status().isOk());
        mockMvc.perform(options("/api/v1/accounts")).andExpect(status().isOk());
    }

    @Test
    void missingTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("{\"message\":\"A bearer token is required\"}"));
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/customers/1").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("{\"message\":\"The token is invalid or expired\"}"));
    }

    @Test
    void validTokenReachesTheRoute() throws Exception {
        mockMvc.perform(get("/api/v1/customers/1").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(content().string("passed"));
    }

    private String token() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("ana")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(30, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    @RestController
    static class ProbeController {

        @PostMapping("/api/v1/auth/login")
        ResponseEntity<String> login() {
            return ResponseEntity.ok("passed");
        }

        @GetMapping("/openapi.yaml")
        ResponseEntity<String> contract() {
            return ResponseEntity.ok("passed");
        }

        @RequestMapping(value = "/api/v1/accounts", method = RequestMethod.OPTIONS)
        ResponseEntity<String> options() {
            return ResponseEntity.ok("passed");
        }

        @GetMapping("/api/v1/customers/1")
        ResponseEntity<String> customer() {
            return ResponseEntity.ok("passed");
        }
    }
}
