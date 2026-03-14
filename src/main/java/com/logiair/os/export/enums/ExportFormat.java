package com.logiair.os.export.enums;

public enum ExportFormat {
    EXCEL("excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    PDF("pdf", "application/pdf", ".pdf");

    private final String code;
    private final String mimeType;
    private final String fileExtension;

    ExportFormat(String code, String mimeType, String fileExtension) {
        this.code = code;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    public String getCode() {
        return code;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static ExportFormat fromCode(String code) {
        for (ExportFormat format : values()) {
            if (format.code.equalsIgnoreCase(code)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown export format: " + code);
    }
}
