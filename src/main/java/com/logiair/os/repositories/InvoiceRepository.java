package com.logiair.os.repositories;

import com.logiair.os.models.Invoice;
import com.logiair.os.models.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, Long tenantId);
    boolean existsByInvoiceNumberAndTenantId(String invoiceNumber, Long tenantId);
    Page<Invoice> findByTenantId(Long tenantId, Pageable pageable);
    Page<Invoice> findByTenantIdAndStatus(Long tenantId, InvoiceStatus status, Pageable pageable);
    Page<Invoice> findByTenantIdAndCustomerId(Long tenantId, Long customerId, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND " +
           "i.invoiceDate BETWEEN :startDate AND :endDate")
    Page<Invoice> findByDateRange(@Param("tenantId") Long tenantId, 
                                  @Param("startDate") LocalDate startDate, 
                                  @Param("endDate") LocalDate endDate, 
                                  Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND " +
           "MONTH(i.invoiceDate) = :month AND YEAR(i.invoiceDate) = :year")
    Page<Invoice> findByMonthAndYear(@Param("tenantId") Long tenantId, 
                                     @Param("month") int month, 
                                     @Param("year") int year, 
                                     Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId AND " +
           "i.customer.id = :customerId AND MONTH(i.invoiceDate) = :month AND YEAR(i.invoiceDate) = :year")
    List<Invoice> findByCustomerAndMonthAndYear(@Param("tenantId") Long tenantId, 
                                                @Param("customerId") Long customerId, 
                                                @Param("month") int month, 
                                                @Param("year") int year);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenant.id = :tenantId AND i.status = :status")
    long countByStatus(@Param("tenantId") Long tenantId, @Param("status") InvoiceStatus status);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.tenant.id = :tenantId AND i.status = :status")
    java.math.BigDecimal sumTotalByStatus(@Param("tenantId") Long tenantId, @Param("status") InvoiceStatus status);
    
    @Query("SELECT i.status, COUNT(i), SUM(i.totalAmount) FROM Invoice i WHERE i.tenant.id = :tenantId GROUP BY i.status")
    List<Object[]> getInvoiceStats(@Param("tenantId") Long tenantId);
}
