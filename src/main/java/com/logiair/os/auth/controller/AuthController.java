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

import java.util.HashMap;
import java.util.Map;

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
        logger.info("user" + request.toString());
        String token = authService.register(request);
        return ResponseEntity.ok(Map.of("token", token, "message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token, "message", "Login successful"));
    }

}

