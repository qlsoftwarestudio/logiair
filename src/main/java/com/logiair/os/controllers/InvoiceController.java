package com.logiair.os.controllers;

import com.logiair.os.dto.request.InvoiceRequest;
import com.logiair.os.dto.response.InvoiceResponse;
import com.logiair.os.export.service.PdfExporter;
import com.logiair.os.export.service.ExcelExporter;
import com.logiair.os.models.InvoiceStatus;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.services.InvoiceService;
import com.logiair.os.tenant.TenantContext;
import com.logiair.os.repositories.UserRepository;
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
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final PdfExporter pdfExporter;
    private final ExcelExporter excelExporter;

    public InvoiceController(InvoiceService invoiceService, UserRepository userRepository, PdfExporter pdfExporter, ExcelExporter excelExporter) {
        this.invoiceService = invoiceService;
        this.userRepository = userRepository;
        this.pdfExporter = pdfExporter;
        this.excelExporter = excelExporter;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {
        
        Tenant tenant = TenantContext.getCurrentTenant();
        if (tenant == null) {
            throw new IllegalStateException("Tenant context not found. Cannot create invoice without tenant context.");
        }
        
        // Obtener usuario autenticado desde SecurityContext
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        
        String userEmail = auth.getName();
        com.logiair.os.models.User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));
        
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
    public ResponseEntity<byte[]> exportInvoice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        InvoiceResponse invoice = invoiceService.getInvoiceById(id, tenantId);
        
        byte[] fileContent;
        String contentType;
        String fileExtension;
        String fileName;
        
        try {
            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    fileContent = excelExporter.generateInvoiceExcel(invoice);
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    fileExtension = "xlsx";
                    break;
                case "pdf":
                default:
                    fileContent = pdfExporter.generateInvoicePdf(invoice);
                    contentType = "application/pdf";
                    fileExtension = "pdf";
                    break;
            }
            
            fileName = "Factura_" + invoice.getInvoiceNumber() + "." + fileExtension;
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileContent);
                    
        } catch (Exception e) {
            throw new RuntimeException("Error generating " + format.toUpperCase() + ": " + e.getMessage(), e);
        }
    }
}
