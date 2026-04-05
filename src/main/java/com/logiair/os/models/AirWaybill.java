package com.logiair.os.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "air_waybills")
public class AirWaybill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Column(name = "awb_number", nullable = false, unique = true)
    private String awbNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @NotBlank
    @Column(name = "airline", nullable = false)
    private String airline;

    @NotBlank
    @Column(name = "origin", nullable = false)
    private String origin;

    @NotBlank
    @Column(name = "destination", nullable = false)
    private String destination;

    @NotNull
    @Column(name = "arrival_or_departure_date")
    private LocalDate arrivalOrDepartureDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AirWaybillStatus status;

    @Column(name = "manifest_number")
    private String manifestNumber;

    @Column(name = "pieces")
    private Integer pieces = 0;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg = BigDecimal.ZERO;

    @Column(name = "shipper")
    private String shipper;

    @Column(name = "consignee")
    private String consignee;

    @Enumerated(EnumType.STRING)
    @Column(name = "awb_type")
    private AirWaybillType awbType = AirWaybillType.HOUSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_awb_id")
    private AirWaybill parentAwb;

    @OneToMany(mappedBy = "parentAwb", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AirWaybill> childAwbs = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String observations;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AirWaybill() {}

    public AirWaybill(String awbNumber, OperationType operationType, String airline,
                     String origin, String destination, LocalDate arrivalOrDepartureDate,
                     AirWaybillStatus status, Customer customer, User createdBy, Tenant tenant,
                     Integer pieces, BigDecimal weightKg, String shipper, String consignee) {
        this.awbNumber = awbNumber;
        this.operationType = operationType;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.arrivalOrDepartureDate = arrivalOrDepartureDate;
        this.status = status;
        this.customer = customer;
        this.createdBy = createdBy;
        this.tenant = tenant;
        this.pieces = pieces != null ? pieces : 0;
        this.weightKg = weightKg != null ? weightKg : BigDecimal.ZERO;
        this.shipper = shipper;
        this.consignee = consignee;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
    public OperationType getOperationType() { return operationType; }
    public void setOperationType(OperationType operationType) { this.operationType = operationType; }
    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getArrivalOrDepartureDate() { return arrivalOrDepartureDate; }
    public void setArrivalOrDepartureDate(LocalDate arrivalOrDepartureDate) { this.arrivalOrDepartureDate = arrivalOrDepartureDate; }
    public AirWaybillStatus getStatus() { return status; }
    public void setStatus(AirWaybillStatus status) { this.status = status; }
    public String getManifestNumber() { return manifestNumber; }
    public void setManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; }
    public Integer getPieces() { return pieces; }
    public void setPieces(Integer pieces) { this.pieces = pieces; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public String getShipper() { return shipper; }
    public void setShipper(String shipper) { this.shipper = shipper; }
    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }
    public AirWaybillType getAwbType() { return awbType; }
    public void setAwbType(AirWaybillType awbType) { this.awbType = awbType; }
    public AirWaybill getParentAwb() { return parentAwb; }
    public void setParentAwb(AirWaybill parentAwb) { this.parentAwb = parentAwb; }
    public List<AirWaybill> getChildAwbs() { return childAwbs; }
    public void setChildAwbs(List<AirWaybill> childAwbs) { this.childAwbs = childAwbs; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public String toString() {
        return "AirWaybill{" +
                "id=" + id +
                ", awbNumber='" + awbNumber + '\'' +
                ", awbType=" + awbType +
                ", operationType=" + operationType +
                ", airline='" + airline + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", arrivalOrDepartureDate=" + arrivalOrDepartureDate +
                ", status=" + status +
                ", manifestNumber='" + manifestNumber + '\'' +
                ", pieces=" + pieces +
                ", weightKg=" + weightKg +
                ", shipper='" + shipper + '\'' +
                ", consignee='" + consignee + '\'' +
                ", observations='" + observations + '\'' +
                ", createdBy=" + (createdBy != null ? createdBy.getName() : "null") +
                ", tenant=" + (tenant != null ? tenant.getName() : "null") +
                '}';
    }
}
