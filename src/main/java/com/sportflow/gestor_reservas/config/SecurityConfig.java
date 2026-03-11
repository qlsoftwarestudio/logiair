package com.sportflow.gestor_reservas.config;

import com.sportflow.gestor_reservas.auth.security.TenantContextFilter;
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tenantContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

