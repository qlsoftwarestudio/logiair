package com.logiair.os.auth.service;

import com.logiair.os.exceptions.AuthenticationException;
import com.logiair.os.models.Role;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.tenant.TenantContext;
import com.logiair.os.tenant.service.TenantService;
import com.logiair.os.repositories.UserRepository;
import com.logiair.os.user.dto.UserRequestDTO;
import com.logiair.os.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final TenantService tenantService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       UserMapper userMapper,
                       TenantService tenantService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.tenantService = tenantService;
    }

    public String register(UserRequestDTO userRequest) {

        logger.info("Attempting to register user with email: {}", userRequest.getEmail());

        // Get current tenant from context or extract from JWT if available
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new IllegalStateException("No tenant context found. User registration must be done within a tenant context.");
        }

        // Check if user already exists in current tenant
        if (userRepository.existsByEmailAndTenantId(userRequest.getEmail(), currentTenantId)) {
            logger.error("Registration failed: User with email {} already exists in tenant {}", userRequest.getEmail(), currentTenantId);
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists in this tenant");
        }

        // Get tenant
        Tenant tenant = tenantService.findById(currentTenantId);

        // Create new user from DTO using mapper
        User newUser = userMapper.toEntity(userRequest);
        newUser.setTenant(tenant);
        newUser.setActive(userRequest.isActive()); // Set isActive manually
        
        // Debug logging
        logger.info("Mapped user: {}", newUser);
        logger.info("User email after mapping: '{}'", newUser.getEmail());
        logger.info("User role from DTO: {}", userRequest.getRole());
        logger.info("User role after mapping: {}", newUser.getRole());
        logger.info("User isActive from DTO: {}", userRequest.isActive());
        logger.info("User isActive after setting: {}", newUser.getIsActive());
        
        // Encode password
        String hashedPassword = encoder.encode(userRequest.getPassword());
        newUser.setPassword(hashedPassword);
        
        logger.info("User before save: {}", newUser);

        try {
            User savedUser = userRepository.save(newUser);
            logger.info("User registered successfully: {} with ID: {} in tenant: {}", savedUser.getEmail(), savedUser.getId(), currentTenantId);
            String token = jwtService.generateToken(savedUser.getEmail(), currentTenantId);
            return token;
        } catch (Exception e) {
            logger.error("Failed to save user: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to register user: " + e.getMessage());
        }
    }

    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        Long tenantId = user.getTenant().getId();
        return jwtService.generateToken(email, tenantId);
    }
}

