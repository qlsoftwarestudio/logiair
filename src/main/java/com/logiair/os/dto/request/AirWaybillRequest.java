package com.logiair.os.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logiair.os.models.AirWaybillStatus;
import com.logiair.os.models.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

public class AirWaybillRequest {
    @NotBlank(message = "AWB number is required")
    private String awbNumber;
    
    @NotNull(message = "Operation type is required")
    private OperationType operationType;
    
    @NotBlank(message = "Airline is required")
    private String airline;
    
    @NotBlank(message = "Origin is required")
    private String origin;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Arrival/Departure date is required")
    @FutureOrPresent(message = "Date cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalOrDepartureDate;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private String observations;
    
    private String manifestNumber;

    // Constructors
    public AirWaybillRequest() {}

    public AirWaybillRequest(String awbNumber, OperationType operationType, String airline,
                           String origin, String destination, LocalDate arrivalOrDepartureDate,
                           Long customerId, String observations, String manifestNumber) {
        this.awbNumber = awbNumber;
        this.operationType = operationType;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.arrivalOrDepartureDate = arrivalOrDepartureDate;
        this.customerId = customerId;
        this.observations = observations;
        this.manifestNumber = manifestNumber;
    }

    // Getters and Setters
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
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    
    public String getManifestNumber() { return manifestNumber; }
    public void setManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; }
}
