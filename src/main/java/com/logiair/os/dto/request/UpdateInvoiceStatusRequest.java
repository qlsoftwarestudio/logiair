package com.logiair.os.dto.request;

import com.logiair.os.models.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateInvoiceStatusRequest {
    
    @NotNull(message = "Status is required")
    private InvoiceStatus status;

    public UpdateInvoiceStatusRequest() {}

    public UpdateInvoiceStatusRequest(InvoiceStatus status) {
        this.status = status;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
}
