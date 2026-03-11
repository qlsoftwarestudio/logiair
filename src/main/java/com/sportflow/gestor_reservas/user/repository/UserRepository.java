package com.sportflow.gestor_reservas.user.repository;

import com.sportflow.gestor_reservas.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAll(Pageable pageable);
    Page<User> findByTenantId(Long tenantId, Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    Optional<User> findByEmail(String email);
}

