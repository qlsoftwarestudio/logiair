package com.logiair.os.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @NotBlank
    @Column(nullable = false)
    private String companyName;

    @NotBlank
    @Column(name = "cuit", nullable = false)
    private String taxId;

    @NotBlank
    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @NotBlank
    @Email
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @NotBlank
    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Customer() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Customer(String companyName, String taxId, String contactName, String contactEmail, String contactPhone, String address, Tenant tenant) {
        this.companyName = companyName;
        this.taxId = taxId;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.tenant = tenant;
    }

    // Getters and Setters
    public Long getId() { return id; }
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
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
