package com.sportflow.gestor_reservas.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-testing-purposes");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken("test@example.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractEmailFromToken() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        
        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldValidateToken() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("password")
                .roles("USER")
                .build();
        
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
        
        assertFalse(jwtService.isTokenValid(invalidToken, userDetails));
    }
}
