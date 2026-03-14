package com.logiair.os.config;

import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.models.Role;
import com.logiair.os.repositories.TenantRepository;
import com.logiair.os.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default tenant if not exists
            if (tenantRepository.count() == 0) {
                Tenant defaultTenant = new Tenant("Logiair OS Default");
                tenantRepository.save(defaultTenant);
                
                // Create default admin user
                if (userRepository.count() == 0) {
                    User adminUser = new User(
                        "Admin",
                        "User",
                        "admin@logiair.com",
                        Role.ADMIN,
                        true,
                        defaultTenant
                    );
                    adminUser.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(adminUser);
                    
                    // Create default operator user
                    User operatorUser = new User(
                        "Operator",
                        "User",
                        "operator@logiair.com",
                        Role.OPERATOR_LOGISTICS,
                        true,
                        defaultTenant
                    );
                    operatorUser.setPassword(passwordEncoder.encode("operator123"));
                    userRepository.save(operatorUser);
                    
                    // Create default administration user
                    User adminFinanceUser = new User(
                        "Administration",
                        "User",
                        "finance@logiair.com",
                        Role.ADMINISTRATION,
                        true,
                        defaultTenant
                    );
                    adminFinanceUser.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(adminFinanceUser);
                }
            }
        };
    }
}
