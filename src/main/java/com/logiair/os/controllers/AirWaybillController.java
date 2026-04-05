package com.logiair.os.controllers;

import com.logiair.os.dto.request.AirWaybillRequest;
import com.logiair.os.dto.request.UpdateAirWaybillStatusRequest;
import com.logiair.os.dto.response.AirWaybillResponse;
import com.logiair.os.export.service.ExcelExporter;
import com.logiair.os.models.AirWaybillStatus;
import com.logiair.os.models.AirWaybillType;
import com.logiair.os.models.Tenant;
import com.logiair.os.models.User;
import com.logiair.os.repositories.UserRepository;
import com.logiair.os.services.AirWaybillService;
import com.logiair.os.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/air-waybills")
public class AirWaybillController {

    private final AirWaybillService airWaybillService;
    private final UserRepository userRepository;
    private final ExcelExporter excelExporter;

    public AirWaybillController(AirWaybillService airWaybillService, UserRepository userRepository, ExcelExporter excelExporter) {
        this.airWaybillService = airWaybillService;
        this.userRepository = userRepository;
        this.excelExporter = excelExporter;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<AirWaybillResponse> createAirWaybill(
            @Valid @RequestBody AirWaybillRequest request) {
        
        // Get user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            user = (User) authentication.getPrincipal();
        } else if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            // Handle case where principal is Spring Security User
            org.springframework.security.core.userdetails.User springUser = 
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            
            // Load our User entity from database using the username/email
            String username = springUser.getUsername();
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in database: " + username));
        }
        
        if (user == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        Tenant tenant = TenantContext.getCurrentTenant();
        AirWaybillResponse response = airWaybillService.createAirWaybill(request, user, tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<Page<AirWaybillResponse>> getAllAirWaybills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AirWaybillResponse> airWaybills = airWaybillService.getAllAirWaybills(tenantId, pageable);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<Page<AirWaybillResponse>> searchAirWaybills(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AirWaybillResponse> airWaybills = airWaybillService.searchAirWaybills(tenantId, search, pageable);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<Page<AirWaybillResponse>> getAirWaybillsByStatus(
            @PathVariable AirWaybillStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AirWaybillResponse> airWaybills = airWaybillService.getAirWaybillsByStatus(tenantId, status, pageable);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<Page<AirWaybillResponse>> getAirWaybillsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AirWaybillResponse> airWaybills = airWaybillService.getAirWaybillsByCustomer(tenantId, customerId, pageable);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<AirWaybillResponse> getAirWaybillById(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        AirWaybillResponse airWaybill = airWaybillService.getAirWaybillById(id, tenantId);
        return ResponseEntity.ok(airWaybill);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<AirWaybillResponse> updateAirWaybill(
            @PathVariable Long id,
            @Valid @RequestBody AirWaybillRequest request) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        AirWaybillResponse response = airWaybillService.updateAirWaybill(id, request, tenantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS')")
    public ResponseEntity<AirWaybillResponse> updateAirWaybillStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAirWaybillStatusRequest request) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        AirWaybillResponse response = airWaybillService.updateAirWaybillStatus(id, request, tenantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAirWaybill(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        airWaybillService.deleteAirWaybill(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<List<AirWaybillResponse>> getPendingAirWaybills() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<AirWaybillStatus> pendingStatuses = List.of(
                AirWaybillStatus.PRE_ALERT,
                AirWaybillStatus.AWB_REGISTERED,
                AirWaybillStatus.MANIFEST_DECONSOLIDATED,
                AirWaybillStatus.CUSTOMS_PRESENTED
        );
        
        List<AirWaybillResponse> airWaybills = airWaybillService.getAirWaybillsByStatuses(tenantId, pendingStatuses);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/type/{awbType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<List<AirWaybillResponse>> getAirWaybillsByType(
            @PathVariable AirWaybillType awbType) {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<AirWaybillResponse> airWaybills = airWaybillService.getAirWaybillsByType(tenantId, awbType);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<List<AirWaybillResponse>> getChildAirWaybills(
            @PathVariable Long parentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<AirWaybillResponse> airWaybills = airWaybillService.getChildAirWaybills(tenantId, parentId);
        return ResponseEntity.ok(airWaybills);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<byte[]> exportAirWaybillsToExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) AirWaybillType awbType,
            @RequestParam(required = false) AirWaybillStatus status) {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        
        List<AirWaybillResponse> airWaybills = airWaybillService.getAirWaybillsForExport(
                tenantId, startDate, endDate, customerId, awbType, status);
        
        String customerName = customerId != null ? "Cliente " + customerId : null;
        String awbTypeStr = awbType != null ? awbType.toString() : null;
        
        byte[] excelBytes = excelExporter.generateAirWaybillsExcel(airWaybills, startDate, endDate, customerName, awbTypeStr);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "guias_aereas.xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
