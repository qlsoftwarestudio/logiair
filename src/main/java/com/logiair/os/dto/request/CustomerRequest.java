package com.logiair.os.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CustomerRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Tax ID is required")
    private String taxId;
    
    @NotBlank(message = "Contact name is required")
    private String contactName;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
    
    private String address;

    public CustomerRequest() {}

    // Getters and Setters
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
}
