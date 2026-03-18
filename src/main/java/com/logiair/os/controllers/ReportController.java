package com.logiair.os.controllers;

import com.logiair.os.dto.response.DashboardResponse;
import com.logiair.os.export.enums.ExportFormat;
import com.logiair.os.export.enums.ReportType;
import com.logiair.os.export.exception.ExportException;
import com.logiair.os.export.service.ReportExportService;
import com.logiair.os.services.ReportService;
import com.logiair.os.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    public ReportController(ReportService reportService, ReportExportService reportExportService) {
        this.reportService = reportService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION', 'CUSTOMER')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        Long tenantId = TenantContext.getCurrentTenantId();
        DashboardResponse dashboard = reportService.getDashboardData(tenantId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/operations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<Map<String, Object>> getOperationsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Map<String, Object> report = reportService.getOperationsReport(tenantId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<Map<String, Object>> getCustomerReport() {
        Long tenantId = TenantContext.getCurrentTenantId();
        Map<String, Object> report = reportService.getCustomerReport(tenantId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/invoicing")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<Map<String, Object>> getInvoicingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Map<String, Object> report = reportService.getInvoicingReport(tenantId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/commissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<Map<String, Object>> getCommissionsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Map<String, Object> report = reportService.getCommissionsReport(tenantId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export/{reportType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportType,
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            ReportType type = ReportType.fromCode(reportType);
            ExportFormat exportFormat = ExportFormat.fromCode(format);
            
            Long tenantId = TenantContext.getCurrentTenantId();
            
            byte[] reportData = reportExportService.exportReport(type, exportFormat, tenantId, startDate, endDate);
            
            String fileName = reportExportService.generateFileName(type, exportFormat, tenantId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(reportData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportData);
                    
        } catch (ExportException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
