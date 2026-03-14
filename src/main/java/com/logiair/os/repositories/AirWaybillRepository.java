package com.logiair.os.repositories;

import com.logiair.os.models.AirWaybill;
import com.logiair.os.models.AirWaybillStatus;
import com.logiair.os.models.OperationType;
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
public interface AirWaybillRepository extends JpaRepository<AirWaybill, Long> {
    Optional<AirWaybill> findByAwbNumberAndTenantId(String awbNumber, Long tenantId);
    boolean existsByAwbNumberAndTenantId(String awbNumber, Long tenantId);
    Page<AirWaybill> findByTenantId(Long tenantId, Pageable pageable);
    Page<AirWaybill> findByTenantIdAndStatus(Long tenantId, AirWaybillStatus status, Pageable pageable);
    Page<AirWaybill> findByTenantIdAndCustomerId(Long tenantId, Long customerId, Pageable pageable);
    Page<AirWaybill> findByTenantIdAndOperationType(Long tenantId, OperationType operationType, Pageable pageable);
    
    @Query("SELECT awb FROM AirWaybill awb WHERE awb.tenant.id = :tenantId AND " +
           "(LOWER(awb.awbNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(awb.airline) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(awb.origin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(awb.destination) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AirWaybill> searchAirWaybills(@Param("tenantId") Long tenantId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT awb FROM AirWaybill awb WHERE awb.tenant.id = :tenantId AND " +
           "awb.arrivalOrDepartureDate BETWEEN :startDate AND :endDate")
    Page<AirWaybill> findByDateRange(@Param("tenantId") Long tenantId, 
                                    @Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);
    
    @Query("SELECT COUNT(awb) FROM AirWaybill awb WHERE awb.tenant.id = :tenantId AND awb.status = :status")
    long countByStatus(@Param("tenantId") Long tenantId, @Param("status") AirWaybillStatus status);
    
    @Query("SELECT awb.status, COUNT(awb) FROM AirWaybill awb WHERE awb.tenant.id = :tenantId GROUP BY awb.status")
    List<Object[]> countByStatusGrouped(@Param("tenantId") Long tenantId);
    
    @Query("SELECT awb FROM AirWaybill awb WHERE awb.tenant.id = :tenantId AND awb.status IN :statuses")
    List<AirWaybill> findByStatuses(@Param("tenantId") Long tenantId, @Param("statuses") List<AirWaybillStatus> statuses);
}
