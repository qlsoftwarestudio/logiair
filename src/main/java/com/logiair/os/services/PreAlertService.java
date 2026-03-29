package com.logiair.os.services;

import com.logiair.os.dto.request.PreAlertRequest;
import com.logiair.os.dto.response.PreAlertResponse;
import com.logiair.os.exceptions.ResourceNotFoundException;
import com.logiair.os.mappers.PreAlertMapper;
import com.logiair.os.models.Customer;
import com.logiair.os.models.PreAlert;
import com.logiair.os.models.PreAlertStatus;
import com.logiair.os.models.Tenant;
import com.logiair.os.repositories.CustomerRepository;
import com.logiair.os.repositories.PreAlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PreAlertService {

    private final PreAlertRepository preAlertRepository;
    private final CustomerRepository customerRepository;
    private final PreAlertMapper preAlertMapper;

    public PreAlertService(PreAlertRepository preAlertRepository,
                           CustomerRepository customerRepository,
                           PreAlertMapper preAlertMapper) {
        this.preAlertRepository = preAlertRepository;
        this.customerRepository = customerRepository;
        this.preAlertMapper = preAlertMapper;
    }

    public PreAlertResponse createPreAlert(PreAlertRequest request, Customer customer, Tenant tenant) {
        // Check if AWB already exists for this tenant
        if (preAlertRepository.existsByAwbNumberAndTenantId(request.getAwbNumber(), tenant.getId())) {
            throw new IllegalArgumentException("PreAlert with AWB " + request.getAwbNumber() + " already exists for this tenant");
        }

        PreAlert preAlert = preAlertMapper.toEntity(request);
        preAlert.setCustomer(customer);
        preAlert.setTenant(tenant);
        preAlert.setStatus(PreAlertStatus.PENDING);
        
        PreAlert savedPreAlert = preAlertRepository.save(preAlert);
        return preAlertMapper.toResponse(savedPreAlert);
    }

    @Transactional(readOnly = true)
    public Page<PreAlertResponse> getPreAlertsByTenant(Long tenantId, Pageable pageable) {
        return preAlertRepository.findByTenantId(tenantId, pageable)
                .map(preAlertMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PreAlertResponse> getPreAlertsByCustomer(Long customerId, Long tenantId, Pageable pageable) {
        return preAlertRepository.findByCustomerIdAndTenantId(customerId, tenantId, pageable)
                .map(preAlertMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PreAlertResponse getPreAlertById(Long id, Long tenantId) {
        PreAlert preAlert = preAlertRepository.findById(id)
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("PreAlert not found with id: " + id));
        
        return preAlertMapper.toResponse(preAlert);
    }

    public PreAlertResponse updatePreAlertStatus(Long id, PreAlertStatus status, Long tenantId) {
        PreAlert existingPreAlert = preAlertRepository.findById(id)
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("PreAlert not found with id: " + id));

        existingPreAlert.setStatus(status);
        if (status == PreAlertStatus.COMPLETED) {
            existingPreAlert.setProcessedAt(LocalDateTime.now());
        }

        PreAlert updatedPreAlert = preAlertRepository.save(existingPreAlert);
        return preAlertMapper.toResponse(updatedPreAlert);
    }

    @Transactional(readOnly = true)
    public List<PreAlertResponse> getRecentPreAlerts(Long tenantId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return preAlertRepository.findRecentByTenant(tenantId, since, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(preAlertMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getPreAlertCountByStatus(Long tenantId, PreAlertStatus status) {
        return preAlertRepository.countByStatusAndTenantId(status, tenantId);
    }

    public void deletePreAlert(Long id, Long tenantId) {
        PreAlert preAlert = preAlertRepository.findById(id)
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("PreAlert not found with id: " + id));

        preAlertRepository.delete(preAlert);
    }
}
