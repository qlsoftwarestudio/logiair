package com.logiair.os.controllers;

import com.logiair.os.dto.request.PreAlertRequest;
import com.logiair.os.dto.response.PreAlertResponse;
import com.logiair.os.models.Customer;
import com.logiair.os.models.PreAlertStatus;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.services.CustomerService;
import com.logiair.os.services.PreAlertService;
import com.logiair.os.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prealerts")
public class PreAlertController {

    private final PreAlertService preAlertService;
    private final CustomerService customerService;

    public PreAlertController(PreAlertService preAlertService, CustomerService customerService) {
        this.preAlertService = preAlertService;
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM')") // Solo para n8n/system calls
    public ResponseEntity<Object> createPreAlert(
            @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
            @Valid @RequestBody PreAlertRequest request,
            @AuthenticationPrincipal User user) {
        
        // Determinar tenant del header o contexto
        Long currentTenantId = tenantId != null ? tenantId : TenantContext.getCurrentTenantId();
        
        // Validar que el cliente exista en el tenant
        Customer customer = customerService.getCustomerById(request.getCustomerId(), currentTenantId);
        
        // Validar que tenga AI PreAlerts activado
        if (!customer.getAiPreAlerts()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "AI PreAlerts not enabled for this customer",
                        "customerId", request.getCustomerId(),
                        "message", "El cliente no tiene activada la funcionalidad de PreAlertas IA"
                    ));
        }
        
        Tenant tenant = customer.getTenant();
        PreAlertResponse response = preAlertService.createPreAlert(request, customer, tenant);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<Page<PreAlertResponse>> getAllPreAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PreAlertResponse> preAlerts = preAlertService.getPreAlertsByTenant(tenantId, pageable);
        return ResponseEntity.ok(preAlerts);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<Page<PreAlertResponse>> getPreAlertsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PreAlertResponse> preAlerts = preAlertService.getPreAlertsByCustomer(customerId, tenantId, pageable);
        return ResponseEntity.ok(preAlerts);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<List<PreAlertResponse>> getRecentPreAlerts() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<PreAlertResponse> preAlerts = preAlertService.getRecentPreAlerts(tenantId);
        return ResponseEntity.ok(preAlerts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<PreAlertResponse> getPreAlertById(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        PreAlertResponse preAlert = preAlertService.getPreAlertById(id, tenantId);
        return ResponseEntity.ok(preAlert);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<Object> updatePreAlertStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        PreAlertStatus newStatus;
        
        try {
            newStatus = PreAlertStatus.valueOf(statusUpdate.get("status").toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status", "validStatuses", java.util.Arrays.asList(PreAlertStatus.values())));
        }
        
        PreAlertResponse response = preAlertService.updatePreAlertStatus(id, newStatus, tenantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePreAlert(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        preAlertService.deletePreAlert(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<Map<String, Object>> getPreAlertStats() {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        Map<String, Object> stats = Map.of(
            "totalPending", preAlertService.getPreAlertCountByStatus(tenantId, PreAlertStatus.PENDING),
            "totalProcessing", preAlertService.getPreAlertCountByStatus(tenantId, PreAlertStatus.PROCESSING),
            "totalCompleted", preAlertService.getPreAlertCountByStatus(tenantId, PreAlertStatus.COMPLETED),
            "totalError", preAlertService.getPreAlertCountByStatus(tenantId, PreAlertStatus.ERROR),
            "recent24h", preAlertService.getRecentPreAlerts(tenantId).size()
        );
        
        return ResponseEntity.ok(stats);
    }
}
