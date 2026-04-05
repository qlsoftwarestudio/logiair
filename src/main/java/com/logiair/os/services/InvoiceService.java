package com.logiair.os.services;

import com.logiair.os.dto.request.InvoiceRequest;
import com.logiair.os.dto.request.InvoiceItemRequest;
import com.logiair.os.dto.response.InvoiceResponse;
import com.logiair.os.exceptions.ResourceNotFoundException;
import com.logiair.os.mappers.InvoiceMapper;
import com.logiair.os.models.*;
import com.logiair.os.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CustomerRepository customerRepository;
    private final AirWaybillRepository airWaybillRepository;
    private final TenantRepository tenantRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceRepository invoiceRepository,
                         InvoiceItemRepository invoiceItemRepository,
                         CustomerRepository customerRepository,
                         AirWaybillRepository airWaybillRepository,
                         TenantRepository tenantRepository,
                         InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.customerRepository = customerRepository;
        this.airWaybillRepository = airWaybillRepository;
        this.tenantRepository = tenantRepository;
        this.invoiceMapper = invoiceMapper;
    }

    public InvoiceResponse createInvoice(InvoiceRequest request, User createdBy, Tenant tenant) {
        logger.info("Creating invoice with request: {}, user: {}, tenant: {}", 
                   request.getInvoiceNumber(), createdBy != null ? createdBy.getEmail() : "null", 
                   tenant != null ? tenant.getName() : "null");

        // Validaciones de null
        if (request == null) {
            throw new IllegalArgumentException("Invoice request cannot be null");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by user cannot be null");
        }
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null");
        }

        // Check if invoice number already exists for this tenant
        if (invoiceRepository.existsByInvoiceNumberAndTenantId(request.getInvoiceNumber(), tenant.getId())) {
            throw new IllegalArgumentException("Invoice with number " + request.getInvoiceNumber() + " already exists");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .filter(c -> c.getTenant().getId().equals(tenant.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        logger.info("Found customer: {} for invoice creation", customer.getCompanyName());

        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setTenant(tenant);
        invoice.setCustomer(customer);
        invoice.setCreatedBy(createdBy);

        // Set all required fields before validation
        if (request.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }
        if (invoice.getTotalAmount() == null) {
            invoice.setTotalAmount(BigDecimal.ZERO);
        }

        logger.info("Saving invoice with number: {}", invoice.getInvoiceNumber());
        Invoice savedInvoice = invoiceRepository.save(invoice);
        logger.info("Invoice saved with ID: {}", savedInvoice.getId());
        final Invoice finalSavedInvoice = savedInvoice;
        final Tenant finalTenant = tenant;

        // Create invoice items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<InvoiceItem> items = request.getItems().stream()
                    .map(itemRequest -> createInvoiceItem(itemRequest, finalSavedInvoice, finalTenant))
                    .collect(Collectors.toList());
            
            invoiceItemRepository.saveAll(items);
            
            // Recalculate total amount from items
            BigDecimal calculatedTotal = items.stream()
                    .map(InvoiceItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            savedInvoice.setTotalAmount(calculatedTotal);
            savedInvoice = invoiceRepository.save(savedInvoice);
        }

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(Long tenantId, Pageable pageable) {
        return invoiceRepository.findByTenantId(tenantId, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoicesByStatus(Long tenantId, InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByTenantIdAndStatus(tenantId, status, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoicesByCustomer(Long tenantId, Long customerId, Pageable pageable) {
        return invoiceRepository.findByTenantIdAndCustomerId(tenantId, customerId, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id, Long tenantId) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(inv -> inv.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate, Long customerId, String companyName) {
        List<Invoice> invoices;
        
        if (customerId != null) {
            invoices = invoiceRepository.findByCustomerAndDateRange(tenantId, customerId, startDate, endDate);
        } else if (companyName != null && !companyName.trim().isEmpty()) {
            invoices = invoiceRepository.findByCustomerNameAndDateRange(tenantId, companyName, startDate, endDate);
        } else {
            invoices = invoiceRepository.findByDateRange(tenantId, startDate, endDate, 
                    org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
        
        return invoices.stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request, Long tenantId) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .filter(inv -> inv.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        // Check if another invoice with same number exists
        if (!existingInvoice.getInvoiceNumber().equals(request.getInvoiceNumber()) &&
            invoiceRepository.existsByInvoiceNumberAndTenantId(request.getInvoiceNumber(), tenantId)) {
            throw new IllegalArgumentException("Invoice with number " + request.getInvoiceNumber() + " already exists");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        // Get tenant object
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
        final Tenant finalTenant = tenant;
        final Invoice finalExistingInvoice = existingInvoice;

        // Remove existing invoice items
        invoiceItemRepository.deleteAll(invoiceItemRepository.findByInvoiceId(id));

        invoiceMapper.updateEntityFromRequest(request, existingInvoice);
        existingInvoice.setCustomer(customer);

        // Create new invoice items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<InvoiceItem> items = request.getItems().stream()
                    .map(itemRequest -> createInvoiceItem(itemRequest, finalExistingInvoice, finalTenant))
                    .collect(java.util.stream.Collectors.toList());
            
            invoiceItemRepository.saveAll(items);
            
            // Recalculate total amount from items
            BigDecimal calculatedTotal = items.stream()
                    .map(InvoiceItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            existingInvoice.setTotalAmount(calculatedTotal);
        }

        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);
        return invoiceMapper.toResponse(updatedInvoice);
    }

    public InvoiceResponse updateInvoiceStatus(Long id, InvoiceStatus status, Long tenantId) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(inv -> inv.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.toResponse(updatedInvoice);
    }

    public void deleteInvoice(Long id, Long tenantId) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(inv -> inv.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        // Delete invoice items first
        invoiceItemRepository.deleteAll(invoiceItemRepository.findByInvoiceId(id));
        
        // Delete invoice
        invoiceRepository.delete(invoice);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getInvoiceStats(Long tenantId) {
        return invoiceRepository.getInvoiceStats(tenantId);
    }

    @Transactional(readOnly = true)
    public long getTotalInvoices(Long tenantId) {
        return invoiceRepository.findByTenantId(tenantId, Pageable.unpaged()).getTotalElements();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalInvoicedAmount(Long tenantId) {
        BigDecimal total = invoiceRepository.sumTotalByStatus(tenantId, InvoiceStatus.PAID);
        BigDecimal pending = invoiceRepository.sumTotalByStatus(tenantId, InvoiceStatus.PENDING);
        return (total != null ? total : BigDecimal.ZERO).add(pending != null ? pending : BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public long getPendingInvoicesCount(Long tenantId) {
        return invoiceRepository.countByStatus(tenantId, InvoiceStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long getPaidInvoicesCount(Long tenantId) {
        return invoiceRepository.countByStatus(tenantId, InvoiceStatus.PAID);
    }

    @Transactional(readOnly = true)
    public List<com.logiair.os.dto.response.AirWaybillResponse> getAirWaybillsByManifest(Long tenantId, String manifestNumber) {
        List<AirWaybill> awbs = airWaybillRepository.findByTenantIdAndManifestNumber(tenantId, manifestNumber);
        return awbs.stream()
                .map(awb -> {
                    // Use AirWaybillMapper to convert to response
                    return com.logiair.os.mappers.AirWaybillMapper.INSTANCE.toResponse(awb);
                })
                .toList();
    }

    private InvoiceItem createInvoiceItem(com.logiair.os.dto.request.InvoiceItemRequest request, Invoice invoice, Tenant tenant) {
        logger.info("Creating invoice item for airWaybillId: {}, manifestNumber: {}", 
                   request.getAirWaybillId(), request.getManifestNumber());

        InvoiceItem item = invoiceMapper.toItemEntity(request);
        item.setInvoice(invoice);
        
        // Handle manifest-based billing
        if (request.getManifestNumber() != null && !request.getManifestNumber().isEmpty()) {
            item.setManifestNumber(request.getManifestNumber());
            
            // Find and associate all AWBs with this manifest number
            List<AirWaybill> awbs = airWaybillRepository.findByTenantIdAndManifestNumber(tenant.getId(), request.getManifestNumber());
            if (!awbs.isEmpty()) {
                // Associate the first AWB as primary (for reference)
                item.setAirWaybill(awbs.get(0));
                logger.info("Associated {} AWBs with manifest number: {}", awbs.size(), request.getManifestNumber());
            } else {
                logger.warn("No AWBs found with manifest number: {}", request.getManifestNumber());
            }
        }
        // Set AirWaybill only if airWaybillId is provided and no manifest
        else if (request.getAirWaybillId() != null) {
            AirWaybill airWaybill = airWaybillRepository.findById(request.getAirWaybillId())
                    .filter(awb -> awb.getTenant().getId().equals(tenant.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Air Waybill not found with id: " + request.getAirWaybillId()));
            
            logger.info("Found airWaybill: {} for invoice item creation", airWaybill.getAwbNumber());
            item.setAirWaybill(airWaybill);
        }
        
        return item;
    }
}
