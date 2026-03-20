package com.logiair.os.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class InvoiceItemRequest {
    @NotBlank(message = "Service description is required")
    private String serviceDescription;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @Positive(message = "Agency commission must be positive")
    private BigDecimal agencyCommission;
    
    private Long airWaybillId;

    // Constructors
    public InvoiceItemRequest() {}

    public InvoiceItemRequest(String serviceDescription, BigDecimal amount, 
                             BigDecimal agencyCommission, Long airWaybillId) {
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.agencyCommission = agencyCommission;
        this.airWaybillId = airWaybillId;
    }

    // Getters and Setters
    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getAgencyCommission() { return agencyCommission; }
    public void setAgencyCommission(BigDecimal agencyCommission) { this.agencyCommission = agencyCommission; }
    
    public Long getAirWaybillId() { return airWaybillId; }
    public void setAirWaybillId(Long airWaybillId) { this.airWaybillId = airWaybillId; }
}
