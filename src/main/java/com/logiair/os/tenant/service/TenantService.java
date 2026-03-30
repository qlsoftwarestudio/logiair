package com.logiair.os.tenant.service;

import com.logiair.os.auth.service.TaxIdGenerator;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.TenantStatus;
import com.logiair.os.repositories.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;
    private final TaxIdGenerator taxIdGenerator;

    public TenantService(TenantRepository tenantRepository, TaxIdGenerator taxIdGenerator) {
        this.tenantRepository = tenantRepository;
        this.taxIdGenerator = taxIdGenerator;
    }

    @Transactional
    public Tenant create(String businessName, String taxId) {
        logger.info("Creating new tenant with business name: {} and taxId: {}", businessName, taxId);

        // Validate CUIT format
        if (!taxIdGenerator.isValidCUIT(taxId)) {
            logger.error("Invalid CUIT format: {}", taxId);
            throw new IllegalArgumentException("Invalid CUIT format: " + taxId);
        }

        if (tenantRepository.existsByName(businessName)) {
            logger.error("Tenant with name {} already exists", businessName);
            throw new IllegalArgumentException("Tenant with name '" + businessName + "' already exists");
        }

        if (tenantRepository.existsByTaxId(taxId)) {
            logger.error("Tenant with taxId {} already exists", taxId);
            throw new IllegalArgumentException("Tenant with taxId '" + taxId + "' already exists");
        }

        Tenant tenant = new Tenant(businessName, taxId);
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        logger.info("Tenant created successfully: {} with ID: {}", savedTenant.getName(), savedTenant.getId());

        return savedTenant;
    }

    @Transactional
    public Tenant create(String businessName) {
        logger.info("Creating new tenant with business name: {} (auto-generating taxId)", businessName);

        if (tenantRepository.existsByName(businessName)) {
            logger.error("Tenant with name {} already exists", businessName);
            throw new IllegalArgumentException("Tenant with name '" + businessName + "' already exists");
        }

        // Generate tax ID automatically
        String generatedTaxId = taxIdGenerator.generateTaxId(businessName);
        logger.info("Auto-generated tax ID: {} for business: {}", generatedTaxId, businessName);

        // Ensure uniqueness
        int attempts = 0;
        while (tenantRepository.existsByTaxId(generatedTaxId) && attempts < 10) {
            generatedTaxId = taxIdGenerator.generateTaxId(businessName);
            attempts++;
            logger.warn("Tax ID collision detected, regenerating: {} (attempt {})", generatedTaxId, attempts);
        }

        if (attempts >= 10) {
            logger.error("Failed to generate unique tax ID after 10 attempts");
            throw new RuntimeException("Failed to generate unique tax ID");
        }

        Tenant tenant = new Tenant(businessName, generatedTaxId);
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        logger.info("Tenant created successfully: {} with ID: {} and taxId: {}", savedTenant.getName(), savedTenant.getId(), savedTenant.getTaxId());

        return savedTenant;
    }

    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));
    }

    public Tenant findByName(String name) {
        return tenantRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with name: " + name));
    }
}
