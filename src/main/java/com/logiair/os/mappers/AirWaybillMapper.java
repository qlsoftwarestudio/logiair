package com.logiair.os.mappers;

import com.logiair.os.dto.request.AirWaybillRequest;
import com.logiair.os.dto.response.AirWaybillResponse;
import com.logiair.os.dto.response.CustomerResponse;
import com.logiair.os.models.AirWaybill;
import com.logiair.os.models.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AirWaybillMapper {
    AirWaybillMapper INSTANCE = Mappers.getMapper(AirWaybillMapper.class);

    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerToResponse")
    @Mapping(target = "createdBy", source = "createdBy.name")
    @Mapping(target = "parentAwbId", source = "parentAwb.id")
    @Mapping(target = "parentAwbNumber", source = "parentAwb.awbNumber")
    @Mapping(target = "childAwbs", ignore = true)
    AirWaybillResponse toResponse(AirWaybill airWaybill);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "parentAwb", ignore = true)
    @Mapping(target = "childAwbs", ignore = true)
    AirWaybill toEntity(AirWaybillRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "parentAwb", ignore = true)
    @Mapping(target = "childAwbs", ignore = true)
    void updateEntityFromRequest(AirWaybillRequest request, @MappingTarget AirWaybill airWaybill);
    
    @Named("customerToResponse")
    default CustomerResponse customerToResponse(Customer customer) {
        if (customer == null) return null;
        return CustomerMapper.INSTANCE.toResponse(customer);
    }
}
