package com.logiair.os.services;

import com.logiair.os.dto.request.CustomerRequest;
import com.logiair.os.dto.response.CustomerResponse;
import com.logiair.os.exceptions.ResourceNotFoundException;
import com.logiair.os.mappers.CustomerMapper;
import com.logiair.os.models.Customer;
import com.logiair.os.models.Tenant;
import com.logiair.os.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerResponse createCustomer(CustomerRequest request, Tenant tenant) {
        // Check if customer with same Tax ID already exists for this tenant
        if (customerRepository.existsByTaxIdAndTenantId(request.getTaxId(), tenant.getId())) {
            throw new IllegalArgumentException("Customer with Tax ID " + request.getTaxId() + " already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setTenant(tenant);
        
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Long tenantId, Pageable pageable) {
        return customerRepository.findByTenantId(tenantId, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(Long tenantId, String search, Pageable pageable) {
        return customerRepository.searchCustomers(tenantId, search, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id, Long tenantId) {
        return customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerResponseById(Long id, Long tenantId) {
        Customer customer = getCustomerById(id, tenantId);
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request, Long tenantId) {
        Customer existingCustomer = customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Check if another customer with same Tax ID exists
        if (!existingCustomer.getTaxId().equals(request.getTaxId()) &&
            customerRepository.existsByTaxIdAndTenantId(request.getTaxId(), tenantId)) {
            throw new IllegalArgumentException("Customer with Tax ID " + request.getTaxId() + " already exists");
        }

        customerMapper.updateEntityFromRequest(request, existingCustomer);
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toResponse(updatedCustomer);
    }

    public void deleteCustomer(Long id, Long tenantId) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Check if customer has associated air waybills
        // This would require additional repository method to check
        // For now, we'll allow deletion
        
        customerRepository.delete(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomersList(Long tenantId) {
        return customerRepository.findByTenantId(tenantId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getTotalCustomers(Long tenantId) {
        return customerRepository.countByTenantId(tenantId);
    }

    public CustomerResponse updateCustomerAiConfig(Long id, Map<String, Boolean> aiConfig, Long tenantId) {
        Customer existingCustomer = customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Update AI configuration fields based on provided map
        if (aiConfig.containsKey("aiPreAlerts")) {
            existingCustomer.setAiPreAlerts(aiConfig.get("aiPreAlerts"));
        }
        if (aiConfig.containsKey("aiPdfExtraction")) {
            existingCustomer.setAiPdfExtraction(aiConfig.get("aiPdfExtraction"));
        }
        if (aiConfig.containsKey("aiAutoReports")) {
            existingCustomer.setAiAutoReports(aiConfig.get("aiAutoReports"));
        }
        if (aiConfig.containsKey("aiBillingSuggestions")) {
            existingCustomer.setAiBillingSuggestions(aiConfig.get("aiBillingSuggestions"));
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toResponse(updatedCustomer);
    }
}
