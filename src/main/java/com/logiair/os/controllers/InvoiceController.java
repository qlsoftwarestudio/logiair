package com.logiair.os.controllers;

import com.logiair.os.dto.request.InvoiceRequest;
import com.logiair.os.dto.response.InvoiceResponse;
import com.logiair.os.models.InvoiceStatus;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.services.InvoiceService;
import com.logiair.os.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request,
            @AuthenticationPrincipal User user) {
        
        Tenant tenant = TenantContext.getCurrentTenant();
        InvoiceResponse response = invoiceService.createInvoice(request, user, tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<Page<InvoiceResponse>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvoiceResponse> invoices = invoiceService.getAllInvoices(tenantId, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvoiceResponse> invoices = invoiceService.getInvoicesByStatus(tenantId, status, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<Page<InvoiceResponse>> getInvoicesByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvoiceResponse> invoices = invoiceService.getInvoicesByCustomer(tenantId, customerId, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/customer/{customerId}/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<List<InvoiceResponse>> getMonthlyInvoicesByCustomer(
            @PathVariable Long customerId,
            @RequestParam int month,
            @RequestParam int year) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        List<InvoiceResponse> invoices = invoiceService.getMonthlyInvoicesByCustomer(tenantId, customerId, month, year);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        InvoiceResponse invoice = invoiceService.getInvoiceById(id, tenantId);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        InvoiceResponse response = invoiceService.updateInvoice(id, request, tenantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        invoiceService.deleteInvoice(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<String> generateMonthlyInvoices(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long customerId) {
        
        // This would be a complex operation to generate monthly invoices automatically
        // For now, return a message indicating the feature
        return ResponseEntity.ok("Monthly invoice generation feature would be implemented here");
    }

    @GetMapping("/export/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<String> exportInvoice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format) {
        
        // This would generate PDF or Excel export
        // For now, return a message indicating the feature
        return ResponseEntity.ok("Invoice export feature would be implemented here for format: " + format);
    }
}
