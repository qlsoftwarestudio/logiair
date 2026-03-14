package com.logiair.os.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateAirWaybillStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
    
    private String manifestNumber;
    private String observations;

    // Constructors
    public UpdateAirWaybillStatusRequest() {}

    public UpdateAirWaybillStatusRequest(String status, String manifestNumber, String observations) {
        this.status = status;
        this.manifestNumber = manifestNumber;
        this.observations = observations;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getManifestNumber() { return manifestNumber; }
    public void setManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; }
    
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
