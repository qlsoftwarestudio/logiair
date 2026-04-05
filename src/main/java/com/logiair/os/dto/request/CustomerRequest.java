package com.logiair.os.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CustomerRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Tax ID is required")
    private String taxId;
    
    @NotBlank(message = "Contact is required")
    private String contact;
    
    @NotBlank(message = "Contact name is required")
    private String contactName;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
    
    private String address;
    
    // Email field for database compatibility
    private String email;

    // PreAlert Email Configuration
    private String prealertEmail;

    // AI Configuration Fields (optional for partial updates)
    private Boolean aiPreAlerts;
    private Boolean aiPdfExtraction;
    private Boolean aiAutoReports;
    private Boolean aiBillingSuggestions;

    public CustomerRequest() {}

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    // Alias for cuit (Argentine tax ID format)
    public String getCuit() { return taxId; }
    public void setCuit(String cuit) { this.taxId = cuit; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPrealertEmail() { return prealertEmail; }
    public void setPrealertEmail(String prealertEmail) { this.prealertEmail = prealertEmail; }
    
    // AI Configuration Getters and Setters
    public Boolean getAiPreAlerts() { return aiPreAlerts; }
    public void setAiPreAlerts(Boolean aiPreAlerts) { this.aiPreAlerts = aiPreAlerts; }
    
    public Boolean getAiPdfExtraction() { return aiPdfExtraction; }
    public void setAiPdfExtraction(Boolean aiPdfExtraction) { this.aiPdfExtraction = aiPdfExtraction; }
    
    public Boolean getAiAutoReports() { return aiAutoReports; }
    public void setAiAutoReports(Boolean aiAutoReports) { this.aiAutoReports = aiAutoReports; }
    
    public Boolean getAiBillingSuggestions() { return aiBillingSuggestions; }
    public void setAiBillingSuggestions(Boolean aiBillingSuggestions) { this.aiBillingSuggestions = aiBillingSuggestions; }
}
