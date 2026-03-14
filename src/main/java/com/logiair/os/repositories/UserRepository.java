package com.logiair.os.repositories;

import com.logiair.os.models.User;
import com.logiair.os.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    Optional<User> findByEmail(String email);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    Page<User> findByTenantId(Long tenantId, Pageable pageable);
    Page<User> findByTenantIdAndIsActive(Long tenantId, boolean isActive, Pageable pageable);
    Page<User> findByTenantIdAndRole(Long tenantId, Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("tenantId") Long tenantId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("tenantId") Long tenantId, @Param("role") Role role);
}
