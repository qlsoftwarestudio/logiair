package com.logiair.os.dto.response;

import java.math.BigDecimal;

public class InvoiceItemResponse {
    private Long id;
    private String serviceDescription;
    private BigDecimal amount;
    private BigDecimal agencyCommission;
    private AirWaybillResponse airWaybill;

    // Constructors
    public InvoiceItemResponse() {}

    public InvoiceItemResponse(Long id, String serviceDescription, BigDecimal amount,
                              BigDecimal agencyCommission, AirWaybillResponse airWaybill) {
        this.id = id;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.agencyCommission = agencyCommission;
        this.airWaybill = airWaybill;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getAgencyCommission() { return agencyCommission; }
    public void setAgencyCommission(BigDecimal agencyCommission) { this.agencyCommission = agencyCommission; }
    
    public AirWaybillResponse getAirWaybill() { return airWaybill; }
    public void setAirWaybill(AirWaybillResponse airWaybill) { this.airWaybill = airWaybill; }
}
