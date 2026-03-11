package com.sportflow.gestor_reservas.tenant.service;

import com.sportflow.gestor_reservas.models.Tenant;
import com.sportflow.gestor_reservas.models.TenantStatus;
import com.sportflow.gestor_reservas.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant create(String businessName) {
        logger.info("Creating new tenant with business name: {}", businessName);

        if (tenantRepository.existsByName(businessName)) {
            logger.error("Tenant with name {} already exists", businessName);
            throw new IllegalArgumentException("Tenant with name '" + businessName + "' already exists");
        }

        Tenant tenant = new Tenant(businessName);
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        logger.info("Tenant created successfully: {} with ID: {}", savedTenant.getName(), savedTenant.getId());

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
