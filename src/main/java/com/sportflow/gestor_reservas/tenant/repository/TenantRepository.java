package com.sportflow.gestor_reservas.tenant.repository;

import com.sportflow.gestor_reservas.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    boolean existsByName(String name);
}
