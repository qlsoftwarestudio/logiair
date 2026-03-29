package com.logiair.os.mappers;

import com.logiair.os.dto.request.CustomerRequest;
import com.logiair.os.dto.response.CustomerResponse;
import com.logiair.os.models.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerResponse toResponse(Customer customer);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "aiPreAlerts", defaultValue = "false")
    @Mapping(target = "aiPdfExtraction", defaultValue = "false")
    @Mapping(target = "aiAutoReports", defaultValue = "false")
    @Mapping(target = "aiBillingSuggestions", defaultValue = "false")
    @Mapping(target = "prealertEmail", ignore = true)
    Customer toEntity(CustomerRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "prealertEmail", ignore = true)
    void updateEntityFromRequest(CustomerRequest request, @MappingTarget Customer customer);
}
