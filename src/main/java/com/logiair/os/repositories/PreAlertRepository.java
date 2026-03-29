package com.logiair.os.repositories;

import com.logiair.os.models.PreAlert;
import com.logiair.os.models.PreAlertStatus;
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
    
    Optional<PreAlert> findByTenantIdAndId(Long tenantId, Long id);
    
    Page<PreAlert> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<PreAlert> findByTenantIdAndStatus(Long tenantId, PreAlertStatus status, Pageable pageable);
    
    Page<PreAlert> findByTenantIdAndCustomerId(Long tenantId, Long customerId, Pageable pageable);
    
    boolean existsByAwbNumberAndTenantId(String awbNumber, Long tenantId);
    
    Page<PreAlert> findByCustomerIdAndTenantId(Long customerId, Long tenantId, Pageable pageable);
    
    @Query("SELECT p FROM PreAlert p WHERE p.tenant.id = :tenantId AND " +
           "p.createdAt >= :since ORDER BY p.createdAt DESC")
    Page<PreAlert> findRecentByTenant(@Param("tenantId") Long tenantId, @Param("since") LocalDateTime since, Pageable pageable);
    
    long countByStatusAndTenantId(PreAlertStatus status, Long tenantId);
    
    @Query("SELECT p FROM PreAlert p WHERE p.tenant.id = :tenantId AND " +
           "p.createdAt BETWEEN :startDate AND :endDate")
    Page<PreAlert> findByDateRange(@Param("tenantId") Long tenantId, 
                                  @Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate, 
                                  Pageable pageable);
    
    @Query("SELECT p FROM PreAlert p WHERE p.tenant.id = :tenantId AND " +
           "p.customer.id = :customerId AND p.createdAt BETWEEN :startDate AND :endDate")
    Page<PreAlert> findByCustomerAndDateRange(@Param("tenantId") Long tenantId, 
                                           @Param("customerId") Long customerId, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM PreAlert p WHERE p.tenant.id = :tenantId AND p.status = :status")
    long countByStatus(@Param("tenantId") Long tenantId, @Param("status") PreAlertStatus status);
    
    @Query("SELECT p.status, COUNT(p) FROM PreAlert p WHERE p.tenant.id = :tenantId GROUP BY p.status")
    List<Object[]> getPreAlertStats(@Param("tenantId") Long tenantId);
    
    @Query("SELECT p FROM PreAlert p WHERE p.tenant.id = :tenantId AND " +
           "p.source = :source ORDER BY p.createdAt DESC")
    List<PreAlert> findByTenantIdAndSource(@Param("tenantId") Long tenantId, @Param("source") String source);
}
