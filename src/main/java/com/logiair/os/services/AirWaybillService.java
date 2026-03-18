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
        airWaybill.setOperationType(request.getOperationType());
        airWaybill.setAirline(request.getAirline());
        airWaybill.setOrigin(request.getOrigin());
        airWaybill.setDestination(request.getDestination());
        airWaybill.setArrivalOrDepartureDate(request.getArrivalOrDepartureDate());
        airWaybill.setObservations(request.getObservations());
        airWaybill.setManifestNumber(request.getManifestNumber());
        airWaybill.setTenant(tenant);
        airWaybill.setCustomer(customer);
        airWaybill.setCreatedBy(createdBy);
        airWaybill.setStatus(AirWaybillStatus.PRE_ALERT); // Default status
        
        logger.info("About to save AirWaybill, createdBy: {}", airWaybill.getCreatedBy());

        AirWaybill savedAirWaybill = airWaybillRepository.save(airWaybill);
        return airWaybillMapper.toResponse(savedAirWaybill);
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

        AirWaybill updatedAirWaybill = airWaybillRepository.save(existingAirWaybill);
        return airWaybillMapper.toResponse(updatedAirWaybill);
    }

    public AirWaybillResponse updateAirWaybillStatus(Long id, UpdateAirWaybillStatusRequest request, Long tenantId) {
        AirWaybill airWaybill = airWaybillRepository.findById(id)
                .filter(awb -> awb.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Air Waybill not found with id: " + id));

        try {
            AirWaybillStatus newStatus = AirWaybillStatus.valueOf(request.getStatus().toUpperCase());
            
            // Validate status transition workflow
            validateStatusTransition(airWaybill.getStatus(), newStatus);
            
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
                if (newStatus != AirWaybillStatus.MANIFEST_REGISTERED) {
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
}
