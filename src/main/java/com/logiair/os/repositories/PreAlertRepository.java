package com.logiair.os.repositories;

import com.logiair.os.models.PreAlert;
import com.logiair.os.models.PreAlertStatus;
import com.logiair.os.models.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreAlertRepository extends JpaRepository<PreAlert, Long> {
    
    // Find by tenant
    Page<PreAlert> findByTenantId(Long tenantId, Pageable pageable);
    
    // Find by customer and tenant
    Page<PreAlert> findByCustomerIdAndTenantId(Long customerId, Long tenantId, Pageable pageable);
    
    // Find by AWB number and tenant
    Optional<PreAlert> findByAwbNumberAndTenantId(String awbNumber, Long tenantId);
    
    // Find by status and tenant
    Page<PreAlert> findByStatusAndTenantId(PreAlertStatus status, Long tenantId, Pageable pageable);
    
    // Find recent prealerts (last 24 hours)
    @Query("SELECT p FROM PreAlert p WHERE p.tenant.id = :tenantId AND p.createdAt >= :since")
    List<PreAlert> findRecentByTenant(@Param("tenantId") Long tenantId, @Param("since") LocalDateTime since);
    
    // Count by status and tenant
    @Query("SELECT COUNT(p) FROM PreAlert p WHERE p.tenant.id = :tenantId AND p.status = :status")
    long countByStatusAndTenantId(@Param("status") PreAlertStatus status, @Param("tenantId") Long tenantId);
    
    // Check if AWB exists for tenant
    boolean existsByAwbNumberAndTenantId(String awbNumber, Long tenantId);
}
