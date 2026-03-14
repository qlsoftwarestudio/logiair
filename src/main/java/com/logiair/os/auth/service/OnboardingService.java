package com.logiair.os.auth.service;

import com.logiair.os.auth.dto.OnboardingRequest;
import com.logiair.os.models.Role;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.tenant.service.TenantService;
import com.logiair.os.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingService.class);

    private final TenantService tenantService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public OnboardingService(TenantService tenantService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.tenantService = tenantService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String onboard(OnboardingRequest request) {
        logger.info("Starting onboarding process for business: {}", request.getBusinessName());

        // 1. Create Tenant
        Tenant tenant = tenantService.create(request.getBusinessName());
        logger.info("Tenant created with ID: {}", tenant.getId());

        // 2. Create Admin User
        User adminUser = createAdminUser(tenant, request);
        logger.info("Admin user created with ID: {}", adminUser.getId());

        // 3. Generate JWT token
        String token = jwtService.generateToken(adminUser.getEmail(), tenant.getId());
        logger.info("JWT token generated for admin user");

        return token;
    }

    private User createAdminUser(Tenant tenant, OnboardingRequest request) {
        User admin = new User(
            request.getAdminName(),
            request.getAdminLastname(),
            request.getAdminEmail(),
            Role.ADMIN,
            true,
            tenant
        );

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        admin.setPassword(hashedPassword);

        return userRepository.save(admin);
    }
}
