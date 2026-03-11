package com.sportflow.gestor_reservas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OnboardingRequest {
    
    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;
    
    @NotBlank(message = "Admin name is required")
    @Size(min = 2, max = 50, message = "Admin name must be between 2 and 50 characters")
    private String adminName;
    
    @NotBlank(message = "Admin lastname is required")
    @Size(min = 2, max = 50, message = "Admin lastname must be between 2 and 50 characters")
    private String adminLastname;
    
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Getters and Setters
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminLastname() {
        return adminLastname;
    }

    public void setAdminLastname(String adminLastname) {
        this.adminLastname = adminLastname;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "OnboardingRequest{" +
                "businessName='" + businessName + '\'' +
                ", adminName='" + adminName + '\'' +
                ", adminLastname='" + adminLastname + '\'' +
                ", adminEmail='" + adminEmail + '\'' +
                '}';
    }
}
