package com.logiair.os.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CustomerRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "CUIT is required")
    private String cuit;
    
    @NotBlank(message = "Contact is required")
    private String contact;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Constructors
    public CustomerRequest() {}

    public CustomerRequest(String companyName, String cuit, String contact, String email) {
        this.companyName = companyName;
        this.cuit = cuit;
        this.contact = contact;
        this.email = email;
    }

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
