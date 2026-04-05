package com.logiair.os.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logiair.os.models.AirWaybillStatus;
import com.logiair.os.models.AirWaybillType;
import com.logiair.os.models.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AirWaybillResponse {
    private Long id;
    private String awbNumber;
    private OperationType operationType;
    private String airline;
    private String origin;
    private String destination;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalOrDepartureDate;
    
    private AirWaybillStatus status;
    private String manifestNumber;
    private Integer pieces;
    private BigDecimal weightKg;
    private String shipper;
    private String consignee;
    private AirWaybillType awbType;
    private Long parentAwbId;
    private String parentAwbNumber;
    private List<AirWaybillResponse> childAwbs;
    private String observations;
    private CustomerResponse customer;
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public AirWaybillResponse() {}

    public AirWaybillResponse(Long id, String awbNumber, OperationType operationType, String airline,
                             String origin, String destination, LocalDate arrivalOrDepartureDate,
                             AirWaybillStatus status, String manifestNumber, Integer pieces,
                             BigDecimal weightKg, String shipper, String consignee,
                             AirWaybillType awbType, Long parentAwbId, String parentAwbNumber,
                             List<AirWaybillResponse> childAwbs, String observations,
                             CustomerResponse customer, String createdBy, LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.awbNumber = awbNumber;
        this.operationType = operationType;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.arrivalOrDepartureDate = arrivalOrDepartureDate;
        this.status = status;
        this.manifestNumber = manifestNumber;
        this.pieces = pieces;
        this.weightKg = weightKg;
        this.shipper = shipper;
        this.consignee = consignee;
        this.awbType = awbType;
        this.parentAwbId = parentAwbId;
        this.parentAwbNumber = parentAwbNumber;
        this.childAwbs = childAwbs;
        this.observations = observations;
        this.customer = customer;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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

    public Long getParentAwbId() { return parentAwbId; }
    public void setParentAwbId(Long parentAwbId) { this.parentAwbId = parentAwbId; }

    public String getParentAwbNumber() { return parentAwbNumber; }
    public void setParentAwbNumber(String parentAwbNumber) { this.parentAwbNumber = parentAwbNumber; }

    public List<AirWaybillResponse> getChildAwbs() { return childAwbs; }
    public void setChildAwbs(List<AirWaybillResponse> childAwbs) { this.childAwbs = childAwbs; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    
    public CustomerResponse getCustomer() { return customer; }
    public void setCustomer(CustomerResponse customer) { this.customer = customer; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
