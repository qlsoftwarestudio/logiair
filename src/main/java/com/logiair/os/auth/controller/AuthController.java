package com.logiair.os.auth.controller;

import com.logiair.os.auth.dto.AuthRequest;
import com.logiair.os.auth.dto.OnboardingRequest;
import com.logiair.os.auth.service.AuthService;
import com.logiair.os.auth.service.OnboardingService;
import com.logiair.os.user.dto.UserRequestDTO;
import com.logiair.os.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final OnboardingService onboardingService;

    public AuthController(AuthService authService, OnboardingService onboardingService) {
        this.authService = authService;
        this.onboardingService = onboardingService;
    }

    @PostMapping("/onboarding")
    public ResponseEntity<?> onboarding(@Valid @RequestBody OnboardingRequest request) {
        logger.info("Starting onboarding for business: {}", request.getBusinessName());
        String token = onboardingService.onboard(request);
        return ResponseEntity.ok(Map.of("token", token, "message", "Tenant and admin user created successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDTO request) {
        logger.info("Register request received for user: {}", request.toString());
        logger.info("Current authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        logger.info("User authorities: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        
        String token = authService.register(request);
        return ResponseEntity.ok(Map.of("token", token, "message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token, "message", "Login successful"));
    }

    @PostMapping("/service-token")
    public ResponseEntity<?> createServiceToken() {
        logger.info("Creating service token for n8n automation");
        String serviceToken = authService.createServiceToken("n8n-service");
        return ResponseEntity.ok(Map.of(
            "token", serviceToken,
            "type", "service",
            "expiresIn", "never",
            "permissions", Arrays.asList("READ_CUSTOMERS", "WRITE_PREALERTS", "READ_PREALERTS"),
            "message", "Service token created successfully for n8n automation"
        ));
    }

}

