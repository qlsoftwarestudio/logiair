package com.logiair.os.mappers;

import com.logiair.os.dto.request.PreAlertRequest;
import com.logiair.os.dto.response.PreAlertResponse;
import com.logiair.os.models.PreAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PreAlertMapper {
    PreAlertMapper INSTANCE = Mappers.getMapper(PreAlertMapper.class);

    @Mapping(target = "customerId", expression = "java(preAlert.getCustomer() != null ? preAlert.getCustomer().getId() : null)")
    @Mapping(target = "customerName", expression = "java(preAlert.getCustomer() != null ? preAlert.getCustomer().getCompanyName() : null)")
    @Mapping(target = "status", expression = "java(preAlert.getStatus() != null ? preAlert.getStatus().toString() : null)")
    PreAlertResponse toResponse(PreAlert preAlert);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PreAlert toEntity(PreAlertRequest request);
}
