package com.logiair.os.config;

import com.logiair.os.auth.security.TenantContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TenantContextFilter tenantContextFilter;

    public SecurityConfig(TenantContextFilter tenantContextFilter) {
        this.tenantContextFilter = tenantContextFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .disable()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/health", "/actuator/health").permitAll()
                        .requestMatchers("/api/reports/dashboard").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "ADMINISTRATION")
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/air-waybills/**").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.POST, "/api/air-waybills").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS")
                        .requestMatchers(HttpMethod.PUT, "/api/air-waybills/**").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS")
                        .requestMatchers(HttpMethod.DELETE, "/api/air-waybills/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/**").hasAnyRole("ADMIN", "ADMINISTRATION", "OPERATOR_LOGISTICS")
                        .requestMatchers(HttpMethod.POST, "/api/invoices").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.PUT, "/api/invoices/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(tenantContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

