package com.logiair.os.services;

import com.logiair.os.dto.request.AirWaybillRequest;
import com.logiair.os.dto.request.UpdateAirWaybillStatusRequest;
import com.logiair.os.dto.response.AirWaybillResponse;
import com.logiair.os.exceptions.ResourceNotFoundException;
import com.logiair.os.mappers.AirWaybillMapper;
import com.logiair.os.models.*;
import com.logiair.os.repositories.AirWaybillRepository;
import com.logiair.os.repositories.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AirWaybillService {

    private static final Logger logger = LoggerFactory.getLogger(AirWaybillService.class);

    private final AirWaybillRepository airWaybillRepository;
    private final CustomerRepository customerRepository;
    private final AirWaybillMapper airWaybillMapper;

    public AirWaybillService(AirWaybillRepository airWaybillRepository,
                           CustomerRepository customerRepository,
                           AirWaybillMapper airWaybillMapper) {
        this.airWaybillRepository = airWaybillRepository;
        this.customerRepository = customerRepository;
        this.airWaybillMapper = airWaybillMapper;
    }

    public AirWaybillResponse createAirWaybill(AirWaybillRequest request, User createdBy, Tenant tenant) {
        logger.info("Creating AirWaybill with user: {}, tenant: {}", createdBy != null ? createdBy.getName() : "NULL", tenant != null ? tenant.getName() : "NULL");
        
        // Check if AWB number already exists for this tenant
        if (airWaybillRepository.existsByAwbNumberAndTenantId(request.getAwbNumber(), tenant.getId())) {
            throw new IllegalArgumentException("Air Waybill with number " + request.getAwbNumber() + " already exists");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .filter(c -> c.getTenant().getId().equals(tenant.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        AirWaybill airWaybill = new AirWaybill(); // Create entity manually to avoid validation issues
        airWaybill.setAwbNumber(request.getAwbNumber());
        
        // Map IMPORT to IMPO to match database constraint
        OperationType operationType = request.getOperationType();
        if (operationType == OperationType.IMPORT) {
            operationType = OperationType.IMPO;
        }
        airWaybill.setOperationType(operationType);
        
        airWaybill.setAirline(request.getAirline());
        airWaybill.setOrigin(request.getOrigin());
        airWaybill.setDestination(request.getDestination());
        airWaybill.setArrivalOrDepartureDate(request.getArrivalOrDepartureDate());
        airWaybill.setObservations(request.getObservations());
        airWaybill.setManifestNumber(request.getManifestNumber());
        airWaybill.setPieces(request.getPieces() != null ? request.getPieces() : 0);
        airWaybill.setWeightKg(request.getWeightKg() != null ? request.getWeightKg() : java.math.BigDecimal.ZERO);
        airWaybill.setShipper(request.getShipper());
        airWaybill.setConsignee(request.getConsignee());
        airWaybill.setAwbType(request.getAwbType() != null ? request.getAwbType() : AirWaybillType.HOUSE);
        airWaybill.setTenant(tenant);
        airWaybill.setCustomer(customer);
        
        // Ensure createdBy is properly set
        if (createdBy == null) {
            throw new IllegalStateException("Created by user cannot be null");
        }
        airWaybill.setCreatedBy(createdBy);
        
        airWaybill.setStatus(AirWaybillStatus.PRE_ALERT); // Default status
        
        // Handle parent AWB relationship for HOUSE type
        if (request.getParentAwbId() != null) {
            AirWaybill parentAwb = airWaybillRepository.findById(request.getParentAwbId())
                    .filter(awb -> awb.getTenant().getId().equals(tenant.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Air Waybill not found with id: " + request.getParentAwbId()));
            
            validateParentAwb(parentAwb, airWaybill);
            airWaybill.setParentAwb(parentAwb);
            airWaybill.setAwbType(AirWaybillType.HOUSE);
        }
        
        logger.info("About to save AirWaybill, createdBy ID: {}, createdBy name: {}", 
                   airWaybill.getCreatedBy().getId(), airWaybill.getCreatedBy().getName());
        logger.info("AirWaybill object before save: {}", airWaybill.toString());
        
        try {
            AirWaybill savedAirWaybill = airWaybillRepository.save(airWaybill);
            logger.info("AirWaybill saved successfully with ID: {}", savedAirWaybill.getId());
            return airWaybillMapper.toResponse(savedAirWaybill);
        } catch (Exception e) {
            logger.error("Error saving AirWaybill: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<AirWaybillResponse> getAllAirWaybills(Long tenantId, Pageable pageable) {
        return airWaybillRepository.findByTenantId(tenantId, pageable)
                .map(airWaybillMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AirWaybillResponse> searchAirWaybills(Long tenantId, String search, Pageable pageable) {
        return airWaybillRepository.searchAirWaybills(tenantId, search, pageable)
                .map(airWaybillMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AirWaybillResponse> getAirWaybillsByStatus(Long tenantId, AirWaybillStatus status, Pageable pageable) {
        return airWaybillRepository.findByTenantIdAndStatus(tenantId, status, pageable)
                .map(airWaybillMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AirWaybillResponse> getAirWaybillsByCustomer(Long tenantId, Long customerId, Pageable pageable) {
        return airWaybillRepository.findByTenantIdAndCustomerId(tenantId, customerId, pageable)
                .map(airWaybillMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AirWaybillResponse getAirWaybillById(Long id, Long tenantId) {
        AirWaybill airWaybill = airWaybillRepository.findById(id)
                .filter(awb -> awb.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Air Waybill not found with id: " + id));
        
        return airWaybillMapper.toResponse(airWaybill);
    }

    public AirWaybillResponse updateAirWaybill(Long id, AirWaybillRequest request, Long tenantId) {
        AirWaybill existingAirWaybill = airWaybillRepository.findById(id)
                .filter(awb -> awb.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Air Waybill not found with id: " + id));

        // Check if another AWB with same number exists
        if (!existingAirWaybill.getAwbNumber().equals(request.getAwbNumber()) &&
            airWaybillRepository.existsByAwbNumberAndTenantId(request.getAwbNumber(), tenantId)) {
            throw new IllegalArgumentException("Air Waybill with number " + request.getAwbNumber() + " already exists");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        airWaybillMapper.updateEntityFromRequest(request, existingAirWaybill);
        existingAirWaybill.setCustomer(customer);
        
        // Handle parent AWB relationship update
        if (request.getParentAwbId() != null) {
            if (existingAirWaybill.getParentAwb() == null || 
                !existingAirWaybill.getParentAwb().getId().equals(request.getParentAwbId())) {
                AirWaybill parentAwb = airWaybillRepository.findById(request.getParentAwbId())
                        .filter(awb -> awb.getTenant().getId().equals(tenantId))
                        .orElseThrow(() -> new ResourceNotFoundException("Parent Air Waybill not found with id: " + request.getParentAwbId()));
                
                validateParentAwb(parentAwb, existingAirWaybill);
                existingAirWaybill.setParentAwb(parentAwb);
                existingAirWaybill.setAwbType(AirWaybillType.HOUSE);
            }
        } else {
            // Remove parent relationship if parentAwbId is null
            existingAirWaybill.setParentAwb(null);
            if (existingAirWaybill.getAwbType() == AirWaybillType.HOUSE) {
                existingAirWaybill.setAwbType(AirWaybillType.MASTER);
            }
        }

        AirWaybill updatedAirWaybill = airWaybillRepository.save(existingAirWaybill);
        return airWaybillMapper.toResponse(updatedAirWaybill);
    }

    public AirWaybillResponse updateAirWaybillStatus(Long id, UpdateAirWaybillStatusRequest request, Long tenantId) {
        logger.info("Updating status for AirWaybill ID: {} with status: '{}'", id, request.getStatus());
        
        AirWaybill airWaybill = airWaybillRepository.findById(id)
                .filter(awb -> awb.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Air Waybill not found with id: " + id));

        logger.info("Current status: {}, Requested status: '{}'", airWaybill.getStatus(), request.getStatus());

        try {
            AirWaybillStatus newStatus = AirWaybillStatus.valueOf(request.getStatus().toUpperCase());
            logger.info("Parsed new status: {}", newStatus);
            
            // Validate status transition workflow
            validateStatusTransition(airWaybill.getStatus(), newStatus);
            logger.info("Status transition validated successfully");
            
            airWaybill.setStatus(newStatus);
            
            if (request.getManifestNumber() != null) {
                airWaybill.setManifestNumber(request.getManifestNumber());
            }
            
            if (request.getObservations() != null) {
                airWaybill.setObservations(request.getObservations());
            }

            AirWaybill updatedAirWaybill = airWaybillRepository.save(airWaybill);
            return airWaybillMapper.toResponse(updatedAirWaybill);
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }
    }

    public void deleteAirWaybill(Long id, Long tenantId) {
        AirWaybill airWaybill = airWaybillRepository.findById(id)
                .filter(awb -> awb.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Air Waybill not found with id: " + id));

        // Check if AWB has associated invoices
        // This would require additional repository method to check
        // For now, we'll allow deletion only if status is PRE_ALERT
        
        if (airWaybill.getStatus() != AirWaybillStatus.PRE_ALERT) {
            throw new IllegalStateException("Cannot delete Air Waybill that is not in PRE_ALERT status");
        }

        airWaybillRepository.delete(airWaybill);
    }

    @Transactional(readOnly = true)
    public List<AirWaybillResponse> getAirWaybillsByStatuses(Long tenantId, List<AirWaybillStatus> statuses) {
        return airWaybillRepository.findByStatuses(tenantId, statuses)
                .stream()
                .map(airWaybillMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getTotalAirWaybills(Long tenantId) {
        return airWaybillRepository.findByTenantId(tenantId, Pageable.unpaged()).getTotalElements();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAirWaybillsByStatusCount(Long tenantId) {
        return airWaybillRepository.countByStatusGrouped(tenantId);
    }

    private void validateStatusTransition(AirWaybillStatus currentStatus, AirWaybillStatus newStatus) {
        // Define allowed transitions
        switch (currentStatus) {
            case PRE_ALERT:
                if (newStatus != AirWaybillStatus.AWB_REGISTERED) {
                    throw new IllegalStateException("Cannot transition from PRE_ALERT to " + newStatus);
                }
                break;
            case AWB_REGISTERED:
                if (newStatus != AirWaybillStatus.MANIFEST_DECONSOLIDATED) {
                    throw new IllegalStateException("Cannot transition from AWB_REGISTERED to " + newStatus);
                }
                break;
            case MANIFEST_DECONSOLIDATED:
                if (newStatus != AirWaybillStatus.CUSTOMS_PRESENTED) {
                    throw new IllegalStateException("Cannot transition from MANIFEST_DECONSOLIDATED to " + newStatus);
                }
                break;
            case CUSTOMS_PRESENTED:
                if (newStatus != AirWaybillStatus.CUSTOMS_CLEARED && newStatus != AirWaybillStatus.MANIFEST_REGISTERED) {
                    throw new IllegalStateException("Cannot transition from CUSTOMS_PRESENTED to " + newStatus);
                }
                break;
            case MANIFEST_REGISTERED:
                if (newStatus != AirWaybillStatus.PROCESS_COMPLETED) {
                    throw new IllegalStateException("Cannot transition from MANIFEST_REGISTERED to " + newStatus);
                }
                break;
            case PROCESS_COMPLETED:
                if (newStatus != AirWaybillStatus.INVOICED) {
                    throw new IllegalStateException("Cannot transition from PROCESS_COMPLETED to " + newStatus);
                }
                break;
            case INVOICED:
                throw new IllegalStateException("Cannot transition from INVOICED status");
        }
    }
    
    private void validateParentAwb(AirWaybill parentAwb, AirWaybill childAwb) {
        // Validate that parent is a MASTER type
        if (parentAwb.getAwbType() != AirWaybillType.MASTER) {
            throw new IllegalArgumentException("Parent AWB must be of type MASTER");
        }
        
        // Validate that parent is not the same as child
        if (parentAwb.getId().equals(childAwb.getId())) {
            throw new IllegalArgumentException("An AWB cannot be its own parent");
        }
        
        // Validate that parent doesn't have a parent (no nesting beyond 1 level for now)
        if (parentAwb.getParentAwb() != null) {
            throw new IllegalArgumentException("Parent AWB cannot itself be a HOUSE AWB (no nested hierarchies)");
        }
        
        // Validate same tenant
        if (!parentAwb.getTenant().getId().equals(childAwb.getTenant().getId())) {
            throw new IllegalArgumentException("Parent and child AWBs must belong to the same tenant");
        }
    }
    
    @Transactional(readOnly = true)
    public List<AirWaybillResponse> getAirWaybillsByType(Long tenantId, AirWaybillType awbType) {
        return airWaybillRepository.findByTenantIdAndAwbType(tenantId, awbType)
                .stream()
                .map(airWaybillMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<AirWaybillResponse> getChildAirWaybills(Long tenantId, Long parentAwbId) {
        return airWaybillRepository.findByTenantIdAndParentAwbId(tenantId, parentAwbId)
                .stream()
                .map(airWaybillMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<AirWaybillResponse> getAirWaybillsForExport(Long tenantId, LocalDate startDate, LocalDate endDate,
                                                           Long customerId, AirWaybillType awbType, AirWaybillStatus status) {
        // Get all air waybills for tenant
        List<AirWaybill> allAirWaybills = airWaybillRepository.findByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        return allAirWaybills.stream()
                .filter(awb -> customerId == null || (awb.getCustomer() != null && awb.getCustomer().getId().equals(customerId)))
                .filter(awb -> awbType == null || awb.getAwbType() == awbType)
                .filter(awb -> status == null || awb.getStatus() == status)
                .filter(awb -> {
                    if (startDate == null || endDate == null) return true;
                    if (awb.getArrivalOrDepartureDate() == null) return false;
                    return !awb.getArrivalOrDepartureDate().isBefore(startDate) && 
                           !awb.getArrivalOrDepartureDate().isAfter(endDate);
                })
                .map(airWaybillMapper::toResponse)
                .toList();
    }
}
