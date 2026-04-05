package com.logiair.os.mappers;

import com.logiair.os.dto.request.InvoiceRequest;
import com.logiair.os.dto.request.InvoiceItemRequest;
import com.logiair.os.dto.response.InvoiceResponse;
import com.logiair.os.dto.response.InvoiceItemResponse;
import com.logiair.os.models.Invoice;
import com.logiair.os.models.InvoiceItem;
import com.logiair.os.models.AirWaybill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    InvoiceMapper INSTANCE = Mappers.getMapper(InvoiceMapper.class);

    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerToResponse")
    @Mapping(target = "createdBy", source = "createdBy.name")
    @Mapping(target = "items", source = "invoiceItems", qualifiedByName = "itemsToResponse")
    InvoiceResponse toResponse(Invoice invoice);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Invoice toEntity(InvoiceRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(InvoiceRequest request, @MappingTarget Invoice invoice);
    
    @Mapping(target = "airWaybill", source = "airWaybill", qualifiedByName = "airWaybillToResponse")
    @Mapping(target = "manifestNumber", source = "manifestNumber")
    InvoiceItemResponse toItemResponse(InvoiceItem item);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "airWaybill", ignore = true)
    @Mapping(target = "manifestNumber", source = "manifestNumber")
    InvoiceItem toItemEntity(InvoiceItemRequest request);
    
    @Named("customerToResponse")
    default com.logiair.os.dto.response.CustomerResponse customerToResponse(com.logiair.os.models.Customer customer) {
        if (customer == null) return null;
        return CustomerMapper.INSTANCE.toResponse(customer);
    }
    
    @Named("airWaybillToResponse")
    default com.logiair.os.dto.response.AirWaybillResponse airWaybillToResponse(com.logiair.os.models.AirWaybill airWaybill) {
        if (airWaybill == null) return null;
        return AirWaybillMapper.INSTANCE.toResponse(airWaybill);
    }
    
    @Named("itemsToResponse")
    default List<InvoiceItemResponse> itemsToResponse(List<InvoiceItem> invoiceItems) {
        if (invoiceItems == null) return null;
        return invoiceItems.stream().map(this::toItemResponse).toList();
    }
}
