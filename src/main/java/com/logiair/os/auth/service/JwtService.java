package com.logiair.os.auth.service;

import com.logiair.os.models.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Arrays;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email) {
        return generateToken(email, null);
    }

    public String generateToken(String email, Long tenantId) {
        return generateToken(email, tenantId, null);
    }

    public String generateToken(String email, Long tenantId, Role role) {
        var claimsBuilder = Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);

        if (tenantId != null) {
            claimsBuilder.claim("tenantId", tenantId);
        }

        if (role != null) {
            claimsBuilder.claim("role", role.name());
        }

        return claimsBuilder.compact();
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (MalformedJwtException | SignatureException e) {
            throw new MalformedJwtException("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            throw new MalformedJwtException("JWT token parsing failed: " + e.getMessage());
        }
    }

    public Long extractTenantId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("tenantId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Role extractRole(String token) {
        try {
            String roleString = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
            return Role.valueOf(roleString);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return true; // Consider expired if parsing fails
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateServiceToken(String serviceName, List<String> permissions) {
        logger.info("Generating service token for: {} with permissions: {}", serviceName, permissions);
        
        return Jwts.builder()
                .subject(serviceName)
                .claim("type", "service")
                .claim("permissions", permissions)
                .claim("serviceName", serviceName)
                .issuedAt(new Date())
                // Sin expiración para tokens de servicio
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public List<String> extractPermissions(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("permissions", List.class);
        } catch (Exception e) {
            logger.error("Failed to extract permissions from token: {}", e.getMessage());
            return Arrays.asList();
        }
    }

    public String extractTokenType(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("type", String.class);
        } catch (Exception e) {
            logger.error("Failed to extract token type: {}", e.getMessage());
            return null;
        }
    }

    public boolean isServiceToken(String token) {
        String tokenType = extractTokenType(token);
        return "service".equals(tokenType);
    }
}

