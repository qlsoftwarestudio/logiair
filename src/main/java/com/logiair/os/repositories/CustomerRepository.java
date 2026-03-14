package com.logiair.os.repositories;

import com.logiair.os.models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCuitAndTenantId(String cuit, Long tenantId);
    boolean existsByCuitAndTenantId(String cuit, Long tenantId);
    Page<Customer> findByTenantId(Long tenantId, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND " +
           "(LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.cuit) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.contact) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(@Param("tenantId") Long tenantId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenant.id = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);
}
