package com.logiair.os.export.service;

import com.logiair.os.export.enums.ExportFormat;
import com.logiair.os.export.enums.ReportType;
import com.logiair.os.export.exception.ExportException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ReportExportService {
    
    byte[] exportReport(
            ReportType reportType,
            ExportFormat format,
            Long tenantId,
            LocalDate startDate,
            LocalDate endDate
    ) throws ExportException;
    
    String generateFileName(ReportType reportType, ExportFormat format, Long tenantId);
}
