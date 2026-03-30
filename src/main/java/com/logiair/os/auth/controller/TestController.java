package com.logiair.os.auth.controller;

import com.logiair.os.auth.dto.OnboardingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/onboarding")
    public ResponseEntity<?> testOnboarding(@RequestBody OnboardingRequest request) {
        return ResponseEntity.ok(Map.of(
            "message", "Onboarding request received successfully",
            "businessName", request.getBusinessName(),
            "taxId", request.getTaxId(),
            "adminName", request.getAdminName(),
            "adminEmail", request.getAdminEmail(),
            "hasPassword", request.getPassword() != null && !request.getPassword().isEmpty()
        ));
    }
}
