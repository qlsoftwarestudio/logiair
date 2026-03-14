package com.logiair.os.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardResponse {
    private long totalCustomers;
    private long totalAirWaybills;
    private long totalInvoices;
    private long pendingInvoices;
    private long paidInvoices;
    private BigDecimal totalInvoicedAmount;
    private BigDecimal pendingInvoicedAmount;
    private Map<String, Long> airWaybillsByStatus;
    private Map<String, Long> recentOperations;
    private Map<String, BigDecimal> monthlyInvoicing;

    // Constructors
    public DashboardResponse() {}

    public DashboardResponse(long totalCustomers, long totalAirWaybills, long totalInvoices,
                           long pendingInvoices, long paidInvoices, BigDecimal totalInvoicedAmount,
                           BigDecimal pendingInvoicedAmount, Map<String, Long> airWaybillsByStatus,
                           Map<String, Long> recentOperations, Map<String, BigDecimal> monthlyInvoicing) {
        this.totalCustomers = totalCustomers;
        this.totalAirWaybills = totalAirWaybills;
        this.totalInvoices = totalInvoices;
        this.pendingInvoices = pendingInvoices;
        this.paidInvoices = paidInvoices;
        this.totalInvoicedAmount = totalInvoicedAmount;
        this.pendingInvoicedAmount = pendingInvoicedAmount;
        this.airWaybillsByStatus = airWaybillsByStatus;
        this.recentOperations = recentOperations;
        this.monthlyInvoicing = monthlyInvoicing;
    }

    // Getters and Setters
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    
    public long getTotalAirWaybills() { return totalAirWaybills; }
    public void setTotalAirWaybills(long totalAirWaybills) { this.totalAirWaybills = totalAirWaybills; }
    
    public long getTotalInvoices() { return totalInvoices; }
    public void setTotalInvoices(long totalInvoices) { this.totalInvoices = totalInvoices; }
    
    public long getPendingInvoices() { return pendingInvoices; }
    public void setPendingInvoices(long pendingInvoices) { this.pendingInvoices = pendingInvoices; }
    
    public long getPaidInvoices() { return paidInvoices; }
    public void setPaidInvoices(long paidInvoices) { this.paidInvoices = paidInvoices; }
    
    public BigDecimal getTotalInvoicedAmount() { return totalInvoicedAmount; }
    public void setTotalInvoicedAmount(BigDecimal totalInvoicedAmount) { this.totalInvoicedAmount = totalInvoicedAmount; }
    
    public BigDecimal getPendingInvoicedAmount() { return pendingInvoicedAmount; }
    public void setPendingInvoicedAmount(BigDecimal pendingInvoicedAmount) { this.pendingInvoicedAmount = pendingInvoicedAmount; }
    
    public Map<String, Long> getAirWaybillsByStatus() { return airWaybillsByStatus; }
    public void setAirWaybillsByStatus(Map<String, Long> airWaybillsByStatus) { this.airWaybillsByStatus = airWaybillsByStatus; }
    
    public Map<String, Long> getRecentOperations() { return recentOperations; }
    public void setRecentOperations(Map<String, Long> recentOperations) { this.recentOperations = recentOperations; }
    
    public Map<String, BigDecimal> getMonthlyInvoicing() { return monthlyInvoicing; }
    public void setMonthlyInvoicing(Map<String, BigDecimal> monthlyInvoicing) { this.monthlyInvoicing = monthlyInvoicing; }
}
