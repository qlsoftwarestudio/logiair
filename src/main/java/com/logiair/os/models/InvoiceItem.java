package com.logiair.os.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "air_waybill_id", nullable = false)
    private AirWaybill airWaybill;

    @NotBlank
    @Column(name = "service_description", nullable = false)
    private String serviceDescription;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "agency_commission", precision = 15, scale = 2)
    private BigDecimal agencyCommission;

    public InvoiceItem() {}

    public InvoiceItem(Invoice invoice, AirWaybill airWaybill, String serviceDescription,
                      BigDecimal amount, BigDecimal agencyCommission) {
        this.invoice = invoice;
        this.airWaybill = airWaybill;
        this.serviceDescription = serviceDescription;
        this.amount = amount;
        this.agencyCommission = agencyCommission;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public AirWaybill getAirWaybill() { return airWaybill; }
    public void setAirWaybill(AirWaybill airWaybill) { this.airWaybill = airWaybill; }
    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getAgencyCommission() { return agencyCommission; }
    public void setAgencyCommission(BigDecimal agencyCommission) { this.agencyCommission = agencyCommission; }
}
