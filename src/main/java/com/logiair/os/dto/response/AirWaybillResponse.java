package com.logiair.os.dto.response;

import com.logiair.os.models.AirWaybillStatus;
import com.logiair.os.models.OperationType;

import java.time.LocalDateTime;

public class AirWaybillResponse {
    private Long id;
    private String awbNumber;
    private OperationType operationType;
    private String airline;
    private String origin;
    private String destination;
    private LocalDateTime arrivalOrDepartureDate;
    private AirWaybillStatus status;
    private String manifestNumber;
    private String observations;
    private CustomerResponse customer;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AirWaybillResponse() {}

    public AirWaybillResponse(Long id, String awbNumber, OperationType operationType, String airline,
                             String origin, String destination, LocalDateTime arrivalOrDepartureDate,
                             AirWaybillStatus status, String manifestNumber, String observations,
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
    
    public LocalDateTime getArrivalOrDepartureDate() { return arrivalOrDepartureDate; }
    public void setArrivalOrDepartureDate(LocalDateTime arrivalOrDepartureDate) { this.arrivalOrDepartureDate = arrivalOrDepartureDate; }
    
    public AirWaybillStatus getStatus() { return status; }
    public void setStatus(AirWaybillStatus status) { this.status = status; }
    
    public String getManifestNumber() { return manifestNumber; }
    public void setManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; }
    
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
