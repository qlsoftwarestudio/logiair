package com.logiair.os.repositories;

import com.logiair.os.models.InvoiceItem;
import com.logiair.os.models.Invoice;
import com.logiair.os.models.AirWaybill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
    List<InvoiceItem> findByAirWaybillId(Long airWaybillId);
    
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    List<InvoiceItem> findByInvoice(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT SUM(ii.amount) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    BigDecimal sumAmountByInvoice(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT SUM(ii.agencyCommission) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    BigDecimal sumCommissionByInvoice(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.tenant.id = :tenantId AND ii.airWaybill.id = :airWaybillId")
    List<InvoiceItem> findByTenantAndAirWaybill(@Param("tenantId") Long tenantId, @Param("airWaybillId") Long airWaybillId);
    
    @Query("SELECT COUNT(ii) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    long countItemsByInvoice(@Param("invoiceId") Long invoiceId);
}
