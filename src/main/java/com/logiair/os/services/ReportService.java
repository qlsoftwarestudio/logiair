package com.logiair.os.services;

import com.logiair.os.dto.response.DashboardResponse;
import com.logiair.os.models.*;
import com.logiair.os.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final CustomerRepository customerRepository;
    private final AirWaybillRepository airWaybillRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public ReportService(CustomerRepository customerRepository,
                        AirWaybillRepository airWaybillRepository,
                        InvoiceRepository invoiceRepository,
                        InvoiceItemRepository invoiceItemRepository) {
        this.customerRepository = customerRepository;
        this.airWaybillRepository = airWaybillRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    public DashboardResponse getDashboardData(Long tenantId) {
        // Basic counts
        long totalCustomers = customerRepository.countByTenantId(tenantId);
        long totalAirWaybills = airWaybillRepository.findByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long totalInvoices = invoiceRepository.findByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long pendingInvoices = invoiceRepository.countByStatus(tenantId, InvoiceStatus.PENDING);
        long paidInvoices = invoiceRepository.countByStatus(tenantId, InvoiceStatus.PAID);

        // Financial data
        BigDecimal totalInvoicedAmount = getTotalInvoicedAmount(tenantId);
        BigDecimal pendingInvoicedAmount = invoiceRepository.sumTotalByStatus(tenantId, InvoiceStatus.PENDING);
        if (pendingInvoicedAmount == null) {
            pendingInvoicedAmount = BigDecimal.ZERO;
        }

        // Air waybills by status
        Map<String, Long> airWaybillsByStatus = getAirWaybillsByStatusMap(tenantId);

        // Recent operations (last 7 days)
        Map<String, Long> recentOperations = getRecentOperations(tenantId);

        // Monthly invoicing (last 6 months)
        Map<String, BigDecimal> monthlyInvoicing = getMonthlyInvoicing(tenantId);

        return new DashboardResponse(
                totalCustomers,
                totalAirWaybills,
                totalInvoices,
                pendingInvoices,
                paidInvoices,
                totalInvoicedAmount,
                pendingInvoicedAmount,
                airWaybillsByStatus,
                recentOperations,
                monthlyInvoicing
        );
    }

    public Map<String, Object> getOperationsReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        List<AirWaybill> airWaybills = airWaybillRepository.findByDateRange(
                tenantId, startDate.atStartOfDay(), endDate.atTime(23, 59), 
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        Map<String, Object> report = new HashMap<>();
        report.put("totalOperations", airWaybills.size());
        report.put("period", startDate + " to " + endDate);
        
        // Count by operation type
        Map<String, Long> byOperationType = new HashMap<>();
        byOperationType.put("IMPO", airWaybills.stream().filter(awb -> awb.getOperationType() == OperationType.IMPO).count());
        byOperationType.put("EXPO", airWaybills.stream().filter(awb -> awb.getOperationType() == OperationType.EXPO).count());
        report.put("byOperationType", byOperationType);
        
        // Count by status
        Map<String, Long> byStatus = new HashMap<>();
        for (AirWaybillStatus status : AirWaybillStatus.values()) {
            long count = airWaybills.stream().filter(awb -> awb.getStatus() == status).count();
            if (count > 0) {
                byStatus.put(status.name(), count);
            }
        }
        report.put("byStatus", byStatus);
        
        return report;
    }

    public Map<String, Object> getCustomerReport(Long tenantId) {
        List<Customer> customers = customerRepository.findByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalCustomers", customers.size());
        
        // Customers with most operations
        Map<String, Long> customerOperations = new HashMap<>();
        for (Customer customer : customers) {
            long operationCount = airWaybillRepository.findByTenantIdAndCustomerId(
                    tenantId, customer.getId(), org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements();
            customerOperations.put(customer.getCompanyName(), operationCount);
        }
        
        // Sort by operation count (top 10)
        List<Map.Entry<String, Long>> topCustomers = customerOperations.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .toList();
        
        report.put("topCustomersByOperations", topCustomers);
        
        return report;
    }

    public Map<String, Object> getInvoicingReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateRange(tenantId, startDate, endDate, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalInvoices", invoices.size());
        report.put("period", startDate + " to " + endDate);
        
        // Financial totals
        BigDecimal totalAmount = invoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalAmount", totalAmount);
        
        // By status
        Map<String, Object> byStatus = new HashMap<>();
        long pendingCount = invoices.stream().filter(inv -> inv.getStatus() == InvoiceStatus.PENDING).count();
        long paidCount = invoices.stream().filter(inv -> inv.getStatus() == InvoiceStatus.PAID).count();
        
        BigDecimal pendingAmount = invoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal paidAmount = invoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        byStatus.put("PENDING", Map.of("count", pendingCount, "amount", pendingAmount));
        byStatus.put("PAID", Map.of("count", paidCount, "amount", paidAmount));
        report.put("byStatus", byStatus);
        
        return report;
    }

    public Map<String, Object> getCommissionsReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateRange(tenantId, startDate, endDate, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        BigDecimal totalCommissions = BigDecimal.ZERO;
        
        for (Invoice invoice : invoices) {
            BigDecimal invoiceCommissions = invoiceItemRepository.sumCommissionByInvoice(invoice.getId());
            if (invoiceCommissions != null) {
                totalCommissions = totalCommissions.add(invoiceCommissions);
            }
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate + " to " + endDate);
        report.put("totalCommissions", totalCommissions);
        report.put("invoiceCount", invoices.size());
        
        return report;
    }

    private Map<String, Long> getAirWaybillsByStatusMap(Long tenantId) {
        List<Object[]> results = airWaybillRepository.countByStatusGrouped(tenantId);
        Map<String, Long> statusMap = new HashMap<>();
        
        for (Object[] result : results) {
            AirWaybillStatus status = (AirWaybillStatus) result[0];
            Long count = (Long) result[1];
            statusMap.put(status.name(), count);
        }
        
        return statusMap;
    }

    private Map<String, Long> getRecentOperations(Long tenantId) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        List<AirWaybill> recentAirWaybills = airWaybillRepository.findByDateRange(
                tenantId, sevenDaysAgo.atStartOfDay(), LocalDate.now().atTime(23, 59),
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
        
        Map<String, Long> operations = new HashMap<>();
        operations.put("last7Days", (long) recentAirWaybills.size());
        
        // Daily breakdown for last 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            List<AirWaybill> dayAirWaybills = airWaybillRepository.findByDateRange(
                    tenantId, date.atStartOfDay(), date.atTime(23, 59),
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
            
            operations.put(date.toString(), (long) dayAirWaybills.size());
        }
        
        return operations;
    }

    private Map<String, BigDecimal> getMonthlyInvoicing(Long tenantId) {
        Map<String, BigDecimal> monthlyInvoicing = new HashMap<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 0; i < 6; i++) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            int month = yearMonth.getMonthValue();
            int year = yearMonth.getYear();
            
            List<Invoice> monthInvoices = invoiceRepository.findByMonthAndYear(tenantId, month, year, 
                    org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            BigDecimal monthTotal = monthInvoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            monthlyInvoicing.put(yearMonth.toString(), monthTotal);
        }
        
        return monthlyInvoicing;
    }

    private BigDecimal getTotalInvoicedAmount(Long tenantId) {
        BigDecimal paid = invoiceRepository.sumTotalByStatus(tenantId, InvoiceStatus.PAID);
        BigDecimal pending = invoiceRepository.sumTotalByStatus(tenantId, InvoiceStatus.PENDING);
        
        BigDecimal total = BigDecimal.ZERO;
        if (paid != null) {
            total = total.add(paid);
        }
        if (pending != null) {
            total = total.add(pending);
        }
        
        return total;
    }
}
