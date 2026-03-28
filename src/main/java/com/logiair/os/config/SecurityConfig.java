package com.logiair.os.config;

import com.logiair.os.auth.security.TenantContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TenantContextFilter tenantContextFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(TenantContextFilter tenantContextFilter, CorsConfigurationSource corsConfigurationSource) {
        this.tenantContextFilter = tenantContextFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .disable()
                )
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/auth/**", "/health", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "ADMINISTRATION", "SERVICE")
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/air-waybills/**").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "ADMINISTRATION", "CUSTOMER", "SERVICE")
                        .requestMatchers(HttpMethod.POST, "/api/air-waybills").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "SERVICE")
                        .requestMatchers(HttpMethod.PUT, "/api/air-waybills/**").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "SERVICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/air-waybills/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/**").hasAnyRole("ADMIN", "ADMINISTRATION", "OPERATOR_LOGISTICS", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/invoices").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.PUT, "/api/invoices/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/prealerts/**").hasAnyRole("ADMIN", "ADMINISTRATION", "OPERATOR_LOGISTICS", "SERVICE")
                        .requestMatchers(HttpMethod.POST, "/api/prealerts").hasAnyRole("ADMIN", "ADMINISTRATION", "SERVICE")
                        .requestMatchers(HttpMethod.PUT, "/api/prealerts/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/prealerts/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/dashboard").hasAnyRole("ADMIN", "OPERATOR_LOGISTICS", "ADMINISTRATION", "CUSTOMER")
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ADMINISTRATION")
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(tenantContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
