package com.logiair.os.export.service;

import com.logiair.os.dto.response.DashboardResponse;
import com.logiair.os.export.enums.ExportFormat;
import com.logiair.os.export.enums.ReportType;
import com.logiair.os.export.exception.ExportException;
import com.logiair.os.services.ReportService;
import com.logiair.os.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ReportExportServiceImpl implements ReportExportService {
    
    private final ReportService reportService;
    private final ExcelExporter excelExporter;
    
    public ReportExportServiceImpl(ReportService reportService, ExcelExporter excelExporter) {
        this.reportService = reportService;
        this.excelExporter = excelExporter;
    }
    
    @Override
    public byte[] exportReport(
            ReportType reportType,
            ExportFormat format,
            Long tenantId,
            LocalDate startDate,
            LocalDate endDate
    ) throws ExportException {
        
        try {
            Map<String, Object> reportData = getReportData(reportType, tenantId, startDate, endDate);
            
            return switch (format) {
                case EXCEL -> excelExporter.exportToExcel(reportType, reportData);
                case PDF -> throw new ExportException("PDF export not implemented yet");
            };
            
        } catch (Exception e) {
            throw new ExportException(reportType.getDescription(), format.getCode(), e.getMessage());
        }
    }
    
    @Override
    public String generateFileName(ReportType reportType, ExportFormat format, Long tenantId) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("%s_T%d_%s%s", 
                reportType.getCode(), 
                tenantId, 
                timestamp, 
                format.getFileExtension());
    }
    
    private Map<String, Object> getReportData(
            ReportType reportType, 
            Long tenantId, 
            LocalDate startDate, 
            LocalDate endDate
    ) {
        return switch (reportType) {
            case DASHBOARD, GENERAL -> convertDashboardToMap(reportService.getDashboardData(tenantId));
            case OPERATIONS -> reportService.getOperationsReport(tenantId, startDate, endDate);
            case CUSTOMERS -> reportService.getCustomerReport(tenantId);
            case INVOICING, FINANCIAL -> reportService.getInvoicingReport(tenantId, startDate, endDate);
            case COMMISSIONS -> reportService.getCommissionsReport(tenantId, startDate, endDate);
        };
    }
    
    private Map<String, Object> convertDashboardToMap(DashboardResponse dashboard) {
        return Map.of(
                "totalCustomers", dashboard.getTotalCustomers(),
                "totalAirWaybills", dashboard.getTotalAirWaybills(),
                "totalInvoices", dashboard.getTotalInvoices(),
                "pendingInvoices", dashboard.getPendingInvoices(),
                "paidInvoices", dashboard.getPaidInvoices(),
                "totalInvoicedAmount", dashboard.getTotalInvoicedAmount(),
                "pendingInvoicedAmount", dashboard.getPendingInvoicedAmount(),
                "airWaybillsByStatus", dashboard.getAirWaybillsByStatus(),
                "recentOperations", dashboard.getRecentOperations(),
                "monthlyInvoicing", dashboard.getMonthlyInvoicing()
        );
    }
}
