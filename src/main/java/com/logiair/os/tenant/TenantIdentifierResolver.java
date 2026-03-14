package com.logiair.os.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenantId() != null ? 
               TenantContext.getCurrentTenantId().toString() : "default";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
