package com.logiair.os.auth.security;

import com.logiair.os.auth.service.JwtService;
import com.logiair.os.models.Role;
import com.logiair.os.models.Tenant;
import com.logiair.os.repositories.TenantRepository;
import com.logiair.os.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TenantRepository tenantRepository;

    public TenantContextFilter(JwtService jwtService, UserDetailsService userDetailsService, TenantRepository tenantRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tenantRepository = tenantRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Skip JWT processing only for onboarding and login, but process for register
        String path = request.getRequestURI();
        logger.info("TenantContextFilter processing path: {}", path);
        
        if (path.equals("/auth/onboarding") || path.equals("/auth/login") || path.equals("/health")) {
            logger.info("Skipping JWT processing for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractEmail(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Extraer y establecer tenantId del JWT
                Long tenantId = jwtService.extractTenantId(jwt);
                Role role = jwtService.extractRole(jwt);
                
                logger.info("JWT extraction - tenantId: {}, role: {}, userEmail: {}", tenantId, role, userEmail);
                
                if (tenantId != null) {
                    // 🔥 FIX: Obtener el tenant completo de la base de datos
                    Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
                    if (tenant != null) {
                        TenantContext.setCurrentTenant(tenant);
                        logger.info("Tenant context set to: {} ({})", tenant.getName(), tenantId);
                    } else {
                        logger.warn("Tenant with ID {} not found", tenantId);
                    }
                } else {
                    logger.warn("JWT token does not contain tenantId for user: {} with role: {}", userEmail, role);
                }
                
                if (role != null) {
                    logger.info("User role from JWT: {}", role);
                } else {
                    logger.warn("JWT token does not contain role");
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar el context al final del request
            TenantContext.clear();
        }
    }
}
