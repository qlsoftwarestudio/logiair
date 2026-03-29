package com.logiair.os.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CustomerResponse {
    private Long id;
    private String companyName;
    private String taxId;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // AI Configuration Fields
    private Boolean aiPreAlerts;
    private Boolean aiPdfExtraction;
    private Boolean aiAutoReports;
    private Boolean aiBillingSuggestions;
    private String prealertEmail;

    // Constructors
    public CustomerResponse() {}

    public CustomerResponse(Long id, String companyName, String taxId, String contactName, 
                           String contactEmail, String contactPhone, String address,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.companyName = companyName;
        this.taxId = taxId;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // AI Configuration Getters and Setters
    public Boolean getAiPreAlerts() { return aiPreAlerts; }
    public void setAiPreAlerts(Boolean aiPreAlerts) { this.aiPreAlerts = aiPreAlerts; }
    
    public Boolean getAiPdfExtraction() { return aiPdfExtraction; }
    public void setAiPdfExtraction(Boolean aiPdfExtraction) { this.aiPdfExtraction = aiPdfExtraction; }
    
    public Boolean getAiAutoReports() { return aiAutoReports; }
    public void setAiAutoReports(Boolean aiAutoReports) { this.aiAutoReports = aiAutoReports; }
    
    public Boolean getAiBillingSuggestions() { return aiBillingSuggestions; }
    public void setAiBillingSuggestions(Boolean aiBillingSuggestions) { this.aiBillingSuggestions = aiBillingSuggestions; }
    
    public String getPrealertEmail() { return prealertEmail; }
    public void setPrealertEmail(String prealertEmail) { this.prealertEmail = prealertEmail; }
}
