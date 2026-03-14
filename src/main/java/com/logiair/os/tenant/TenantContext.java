package com.logiair.os.tenant;

public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<com.logiair.os.models.Tenant> currentTenantObject = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getCurrentTenantId() {
        return currentTenant.get();
    }

    public static com.logiair.os.models.Tenant getCurrentTenant() {
        return currentTenantObject.get();
    }

    public static void setCurrentTenant(com.logiair.os.models.Tenant tenant) {
        currentTenantObject.set(tenant);
        currentTenant.set(tenant.getId());
    }

    public static void clear() {
        currentTenant.remove();
        currentTenantObject.remove();
    }
}
