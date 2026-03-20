package com.logiair.os.export.service;

import com.logiair.os.export.enums.ReportType;
import com.logiair.os.export.exception.ExportException;
import com.logiair.os.dto.response.InvoiceResponse;
import com.logiair.os.dto.response.InvoiceItemResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExporter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public byte[] exportToExcel(ReportType reportType, Map<String, Object> data) throws ExportException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(reportType.getDescription());
            
            switch (reportType) {
                case DASHBOARD -> createDashboardSheet(workbook, sheet, data);
                case OPERATIONS -> createOperationsSheet(workbook, sheet, data);
                case CUSTOMERS -> createCustomersSheet(workbook, sheet, data);
                case INVOICING -> createInvoicingSheet(workbook, sheet, data);
                case COMMISSIONS -> createCommissionsSheet(workbook, sheet, data);
            }
            
            autoSizeColumns(sheet, getColumnCount(reportType));
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new ExportException("Error generating Excel file: " + e.getMessage(), e);
        }
    }
    
    public byte[] generateInvoiceExcel(InvoiceResponse invoice) throws ExportException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Factura " + invoice.getInvoiceNumber());
            
            int rowNum = createInvoiceSheet(workbook, sheet, invoice);
            
            autoSizeColumns(sheet, 4);
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new ExportException("Error generating invoice Excel file: " + e.getMessage(), e);
        }
    }
    
    private int createInvoiceSheet(XSSFWorkbook workbook, Sheet sheet, InvoiceResponse invoice) {
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("FACTURA");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));
        
        rowNum++; // Espacio
        
        // Información de la factura
        rowNum = createInvoiceInfoSection(workbook, sheet, rowNum, invoice);
        
        rowNum++; // Espacio
        
        // Información del cliente
        rowNum = createCustomerInfoSection(workbook, sheet, rowNum, invoice);
        
        rowNum++; // Espacio
        
        // Items de la factura
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            rowNum = createItemsSection(workbook, sheet, rowNum, invoice);
        }
        
        rowNum++; // Espacio
        
        // Observaciones
        if (invoice.getObservations() != null && !invoice.getObservations().trim().isEmpty()) {
            rowNum = createObservationsSection(workbook, sheet, rowNum, invoice.getObservations());
            rowNum++; // Espacio
        }
        
        // Total
        createTotalSection(workbook, sheet, rowNum, invoice);
        
        return rowNum;
    }
    
    private int createInvoiceInfoSection(XSSFWorkbook workbook, Sheet sheet, int rowNum, InvoiceResponse invoice) {
        createSectionTitle(workbook, sheet, rowNum++, "Información de la Factura");
        
        addKeyValueRow(workbook, sheet, rowNum++, "Número de Factura:", invoice.getInvoiceNumber());
        addKeyValueRow(workbook, sheet, rowNum++, "Fecha:", invoice.getInvoiceDate().format(DATE_FORMATTER));
        addKeyValueRow(workbook, sheet, rowNum++, "Estado:", invoice.getStatus().toString());
        
        return rowNum;
    }
    
    private int createCustomerInfoSection(XSSFWorkbook workbook, Sheet sheet, int rowNum, InvoiceResponse invoice) {
        createSectionTitle(workbook, sheet, rowNum++, "Datos del Cliente");
        
        addKeyValueRow(workbook, sheet, rowNum++, "Cliente:", invoice.getCustomer().getCompanyName());
        
        return rowNum;
    }
    
    private int createItemsSection(XSSFWorkbook workbook, Sheet sheet, int rowNum, InvoiceResponse invoice) {
        createSectionTitle(workbook, sheet, rowNum++, "Detalle de Servicios");
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Cant.");
        headerRow.createCell(1).setCellValue("Descripción");
        headerRow.createCell(2).setCellValue("Precio Unitario");
        headerRow.createCell(3).setCellValue("Total");
        
        // Apply header style
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(createTableHeaderStyle(workbook));
        }
        
        // Items
        for (InvoiceItemResponse item : invoice.getItems()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue(item.getServiceDescription());
            
            Cell priceCell = row.createCell(2);
            priceCell.setCellValue(item.getAmount().doubleValue());
            priceCell.setCellStyle(createCurrencyStyle(workbook));
            
            Cell totalCell = row.createCell(3);
            totalCell.setCellValue(item.getAmount().doubleValue());
            totalCell.setCellStyle(createCurrencyStyle(workbook));
        }
        
        return rowNum;
    }
    
    private int createObservationsSection(XSSFWorkbook workbook, Sheet sheet, int rowNum, String observations) {
        createSectionTitle(workbook, sheet, rowNum++, "Observaciones");
        
        Row row = sheet.createRow(rowNum++);
        Cell obsCell = row.createCell(0);
        obsCell.setCellValue(observations);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
        
        return rowNum;
    }
    
    private void createTotalSection(XSSFWorkbook workbook, Sheet sheet, int rowNum, InvoiceResponse invoice) {
        Row row = sheet.createRow(rowNum);
        row.createCell(2).setCellValue("TOTAL:");
        
        Cell totalCell = row.createCell(3);
        totalCell.setCellValue(invoice.getTotalAmount().doubleValue());
        totalCell.setCellStyle(createHeaderStyle(workbook));
        totalCell.setCellStyle(createCurrencyStyle(workbook));
        
        // Make total bold
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle totalStyle = createCurrencyStyle(workbook);
        totalStyle.setFont(font);
        totalCell.setCellStyle(totalStyle);
    }
    
    private void createDashboardSheet(XSSFWorkbook workbook, Sheet sheet, Map<String, Object> data) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Dashboard General");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        
        rowNum++; // Empty row
        
        // Summary section
        createSectionTitle(workbook, sheet, rowNum++, "Resumen General");
        
        addKeyValueRow(workbook, sheet, rowNum++, "Total Clientes", getLongValue(data.get("totalCustomers")));
        addKeyValueRow(workbook, sheet, rowNum++, "Total Air Waybills", getLongValue(data.get("totalAirWaybills")));
        addKeyValueRow(workbook, sheet, rowNum++, "Total Facturas", getLongValue(data.get("totalInvoices")));
        addKeyValueRow(workbook, sheet, rowNum++, "Facturas Pendientes", getLongValue(data.get("pendingInvoices")));
        addKeyValueRow(workbook, sheet, rowNum++, "Facturas Pagadas", getLongValue(data.get("paidInvoices")));
        
        rowNum++; // Empty row
        
        // Financial section
        createSectionTitle(workbook, sheet, rowNum++, "Información Financiera");
        
        addKeyValueRow(workbook, sheet, rowNum++, "Monto Total Facturado", getBigDecimalValue(data.get("totalInvoicedAmount")));
        addKeyValueRow(workbook, sheet, rowNum++, "Monto Pendiente", getBigDecimalValue(data.get("pendingInvoicedAmount")));
        
        rowNum++; // Empty row
        
        // Status breakdown
        createSectionTitle(workbook, sheet, rowNum++, "Air Waybills por Estado");
        Map<String, Long> statusMap = (Map<String, Long>) data.get("airWaybillsByStatus");
        if (statusMap != null) {
            for (Map.Entry<String, Long> entry : statusMap.entrySet()) {
                addKeyValueRow(workbook, sheet, rowNum++, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void createOperationsSheet(XSSFWorkbook workbook, Sheet sheet, Map<String, Object> data) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Operaciones");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        
        rowNum++; // Empty row
        
        // Period
        addKeyValueRow(workbook, sheet, rowNum++, "Período", data.get("period").toString());
        addKeyValueRow(workbook, sheet, rowNum++, "Total Operaciones", getLongValue(data.get("totalOperations")));
        
        rowNum++; // Empty row
        
        // By operation type
        createSectionTitle(workbook, sheet, rowNum++, "Operaciones por Tipo");
        Map<String, Long> byType = (Map<String, Long>) data.get("byOperationType");
        if (byType != null) {
            for (Map.Entry<String, Long> entry : byType.entrySet()) {
                addKeyValueRow(workbook, sheet, rowNum++, entry.getKey(), entry.getValue());
            }
        }
        
        rowNum++; // Empty row
        
        // By status
        createSectionTitle(workbook, sheet, rowNum++, "Operaciones por Estado");
        Map<String, Long> byStatus = (Map<String, Long>) data.get("byStatus");
        if (byStatus != null) {
            for (Map.Entry<String, Long> entry : byStatus.entrySet()) {
                addKeyValueRow(workbook, sheet, rowNum++, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void createCustomersSheet(XSSFWorkbook workbook, Sheet sheet, Map<String, Object> data) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Clientes");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        
        rowNum++; // Empty row
        
        addKeyValueRow(workbook, sheet, rowNum++, "Total Clientes", getLongValue(data.get("totalCustomers")));
        
        rowNum++; // Empty row
        
        // Top customers
        createSectionTitle(workbook, sheet, rowNum++, "Top 10 Clientes por Operaciones");
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Cliente");
        headerRow.createCell(1).setCellValue("Operaciones");
        headerRow.createCell(2).setCellValue("Porcentaje");
        
        // Apply header style
        for (int i = 0; i < 3; i++) {
            headerRow.getCell(i).setCellStyle(createTableHeaderStyle(workbook));
        }
        
        // Data
        List<Map.Entry<String, Long>> topCustomers = (List<Map.Entry<String, Long>>) data.get("topCustomersByOperations");
        if (topCustomers != null) {
            long totalOps = topCustomers.stream().mapToLong(Map.Entry::getValue).sum();
            
            for (Map.Entry<String, Long> entry : topCustomers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
                
                double percentage = totalOps > 0 ? (entry.getValue() * 100.0 / totalOps) : 0;
                row.createCell(2).setCellValue(String.format("%.2f%%", percentage));
            }
        }
    }
    
    private void createInvoicingSheet(XSSFWorkbook workbook, Sheet sheet, Map<String, Object> data) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Facturación");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        
        rowNum++; // Empty row
        
        addKeyValueRow(workbook, sheet, rowNum++, "Período", data.get("period").toString());
        addKeyValueRow(workbook, sheet, rowNum++, "Total Facturas", getLongValue(data.get("totalInvoices")));
        addKeyValueRow(workbook, sheet, rowNum++, "Monto Total", getBigDecimalValue(data.get("totalAmount")));
        
        rowNum++; // Empty row
        
        // By status
        createSectionTitle(workbook, sheet, rowNum++, "Facturas por Estado");
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Estado");
        headerRow.createCell(1).setCellValue("Cantidad");
        headerRow.createCell(2).setCellValue("Monto");
        
        // Apply header style
        for (int i = 0; i < 3; i++) {
            headerRow.getCell(i).setCellStyle(createTableHeaderStyle(workbook));
        }
        
        // Data
        Map<String, Map<String, Object>> byStatus = (Map<String, Map<String, Object>>) data.get("byStatus");
        if (byStatus != null) {
            for (Map.Entry<String, Map<String, Object>> entry : byStatus.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                Map<String, Object> statusData = entry.getValue();
                
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(getLongValue(statusData.get("count")));
                
                BigDecimal amount = getBigDecimalValue(statusData.get("amount"));
                Cell amountCell = row.createCell(2);
                amountCell.setCellValue(amount.doubleValue());
                amountCell.setCellStyle(createCurrencyStyle(workbook));
            }
        }
    }
    
    private void createCommissionsSheet(XSSFWorkbook workbook, Sheet sheet, Map<String, Object> data) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Comisiones");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        
        rowNum++; // Empty row
        
        addKeyValueRow(workbook, sheet, rowNum++, "Período", data.get("period").toString());
        addKeyValueRow(workbook, sheet, rowNum++, "Total Comisiones", getBigDecimalValue(data.get("totalCommissions")));
        addKeyValueRow(workbook, sheet, rowNum++, "Cantidad de Facturas", getLongValue(data.get("invoiceCount")));
    }
    
    // Helper methods
    private void createSectionTitle(XSSFWorkbook workbook, Sheet sheet, int rowNum, String title) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(createSectionStyle(workbook));
    }
    
    private void addKeyValueRow(XSSFWorkbook workbook, Sheet sheet, int rowNum, String key, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(key);
        
        Cell valueCell = row.createCell(1);
        if (value instanceof BigDecimal) {
            valueCell.setCellValue(((BigDecimal) value).doubleValue());
            valueCell.setCellStyle(createCurrencyStyle(workbook));
        } else if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else if (value != null) {
            valueCell.setCellValue(value.toString());
        }
    }
    
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }
    
    private CellStyle createSectionStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle createTableHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private CellStyle createCurrencyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }
    
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private int getColumnCount(ReportType reportType) {
        return switch (reportType) {
            case DASHBOARD, OPERATIONS, COMMISSIONS -> 2;
            case CUSTOMERS, INVOICING -> 3;
        };
    }
    
    private Long getLongValue(Object value) {
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }
    
    private BigDecimal getBigDecimalValue(Object value) {
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return BigDecimal.ZERO;
    }
}
