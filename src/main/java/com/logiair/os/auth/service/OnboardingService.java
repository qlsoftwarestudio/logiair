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
    private final TaxIdGenerator taxIdGenerator;

    public OnboardingService(TenantService tenantService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           TaxIdGenerator taxIdGenerator) {
        this.tenantService = tenantService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.taxIdGenerator = taxIdGenerator;
    }

    @Transactional
    public String onboard(OnboardingRequest request) {
        logger.info("Starting onboarding process for business: {} with taxId: {}", request.getBusinessName(), request.getTaxId());

        // 1. Create Tenant with provided taxId
        Tenant tenant = tenantService.create(request.getBusinessName(), request.getTaxId());
        logger.info("Tenant created with ID: {} and taxId: {}", tenant.getId(), tenant.getTaxId());

        // 2. Create Admin User
        User adminUser = createAdminUser(tenant, request);
        logger.info("Admin user created with ID: {}", adminUser.getId());

        // 3. Generate JWT token
        String token = jwtService.generateToken(adminUser.getEmail(), tenant.getId());
        logger.info("JWT token generated for admin user");

        return token;
    }

    private User createAdminUser(Tenant tenant, OnboardingRequest request) {
        // Handle name splitting - if adminLastname is null, split adminName
        String firstName, lastName;
        
        if (request.getAdminLastname() != null && !request.getAdminLastname().trim().isEmpty()) {
            // Use provided first and last name
            firstName = request.getAdminName();
            lastName = request.getAdminLastname();
        } else {
            // Split full name into first and last name
            String fullName = request.getAdminName().trim();
            String[] nameParts = fullName.split("\\s+", 2);
            
            if (nameParts.length >= 2) {
                firstName = nameParts[0];
                lastName = nameParts[1];
            } else {
                // If only one word provided, use it as first name and empty last name
                firstName = fullName;
                lastName = "";
            }
        }
        
        User admin = new User(
            firstName,
            lastName,
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
