package com.logiair.os.export.exception;

public class ExportException extends RuntimeException {
    
    public ExportException(String message) {
        super(message);
    }
    
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExportException(String reportType, String format, String reason) {
        super(String.format("Error exporting %s report to %s format: %s", reportType, format, reason));
    }
}
