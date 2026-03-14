package com.logiair.os.repositories;

import com.logiair.os.models.Tenant;
import com.logiair.os.models.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    boolean existsByName(String name);
    java.util.List<Tenant> findByStatus(TenantStatus status);
}
