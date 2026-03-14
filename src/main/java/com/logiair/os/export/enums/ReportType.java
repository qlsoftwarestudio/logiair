package com.logiair.os.export.enums;

public enum ReportType {
    DASHBOARD("dashboard", "Dashboard General"),
    OPERATIONS("operations", "Reporte de Operaciones"),
    CUSTOMERS("customers", "Reporte de Clientes"),
    INVOICING("invoicing", "Reporte de Facturación"),
    COMMISSIONS("commissions", "Reporte de Comisiones");

    private final String code;
    private final String description;

    ReportType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ReportType fromCode(String code) {
        for (ReportType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown report type: " + code);
    }
}
