package com.logiair.os.controllers;

import com.logiair.os.dto.request.CustomerRequest;
import com.logiair.os.dto.response.CustomerResponse;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.services.CustomerService;
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
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal User user) {
        
        Tenant tenant = TenantContext.getCurrentTenant();
        CustomerResponse response = customerService.createCustomer(request, tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> customers = customerService.getAllCustomers(tenantId, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<Page<CustomerResponse>> searchCustomers(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> customers = customerService.searchCustomers(tenantId, search, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        CustomerResponse customer = customerService.getCustomerResponseById(id, tenantId);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        CustomerResponse response = customerService.updateCustomer(id, request, tenantId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/ai-config")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<CustomerResponse> updateCustomerAiConfig(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> aiConfig) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        CustomerResponse response = customerService.updateCustomerAiConfig(id, aiConfig, tenantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        customerService.deleteCustomer(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomersList() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<CustomerResponse> customers = customerService.getAllCustomersList(tenantId);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(
            @RequestParam String email,
            @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantIdHeader) {
        
        // Usar tenant del header o del contexto actual
        Long tenantId = tenantIdHeader != null ? tenantIdHeader : TenantContext.getCurrentTenantId();
        
        CustomerResponse customer = customerService.findByContactEmail(email, tenantId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(customer);
    }

    @PatchMapping("/{id}/prealert-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATION')")
    public ResponseEntity<CustomerResponse> updateCustomerPrealertEmail(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String prealertEmail = request.get("prealertEmail");
        if (prealertEmail == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Long tenantId = TenantContext.getCurrentTenantId();
        CustomerResponse response = customerService.updateCustomerPrealertEmail(id, prealertEmail, tenantId);
        return ResponseEntity.ok(response);
    }
}
