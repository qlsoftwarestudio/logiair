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
    
    public byte[] generateDateRangeInvoicesExcel(List<InvoiceResponse> invoices, LocalDate startDate, LocalDate endDate, boolean includeCharts) throws ExportException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Facturas por Rango de Fechas");
            
            int rowNum = createDateRangeInvoicesSheet(workbook, sheet, invoices, startDate, endDate, includeCharts);
            
            autoSizeColumns(sheet, 8);
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new ExportException("Error generating date range invoices Excel file: " + e.getMessage(), e);
        }
    }
    
    private int createDateRangeInvoicesSheet(XSSFWorkbook workbook, Sheet sheet, List<InvoiceResponse> invoices, LocalDate startDate, LocalDate endDate, boolean includeCharts) {
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE FACTURAS POR RANGO DE FECHAS");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));
        
        rowNum++; // Espacio
        
        // Período
        createSectionTitle(workbook, sheet, rowNum++, 
            "Período: " + startDate.format(DATE_FORMATTER) + " a " + endDate.format(DATE_FORMATTER));
        
        rowNum++; // Espacio
        
        // Resumen estadístico
        createSectionTitle(workbook, sheet, rowNum++, "Resumen Estadístico");
        
        BigDecimal totalAmount = invoices.stream()
                .map(InvoiceResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long paidCount = invoices.stream()
                .filter(i -> i.getStatus().toString().equals("PAID"))
                .count();
        
        long pendingCount = invoices.stream()
                .filter(i -> i.getStatus().toString().equals("PENDING"))
                .count();
        
        // Calcular promedio diario
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyAverage = totalAmount.divide(BigDecimal.valueOf(daysBetween), 2, java.math.RoundingMode.HALF_UP);
        
        addKeyValueRow(workbook, sheet, rowNum++, "Total Facturas:", invoices.size());
        addKeyValueRow(workbook, sheet, rowNum++, "Facturas Pagadas:", paidCount);
        addKeyValueRow(workbook, sheet, rowNum++, "Facturas Pendientes:", pendingCount);
        addKeyValueRow(workbook, sheet, rowNum++, "Monto Total:", totalAmount);
        addKeyValueRow(workbook, sheet, rowNum++, "Promedio Diario:", dailyAverage);
        
        rowNum++; // Espacio
        
        // Tabla de facturas
        createSectionTitle(workbook, sheet, rowNum++, "Detalle de Facturas");
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("N° Factura");
        headerRow.createCell(1).setCellValue("Fecha");
        headerRow.createCell(2).setCellValue("Cliente");
        headerRow.createCell(3).setCellValue("Estado");
        headerRow.createCell(4).setCellValue("Números de Guía");
        headerRow.createCell(5).setCellValue("Cantidad Items");
        headerRow.createCell(6).setCellValue("Total");
        headerRow.createCell(7).setCellValue("Días Transcurridos");
        
        // Apply header style
        for (int i = 0; i < 8; i++) {
            headerRow.getCell(i).setCellStyle(createTableHeaderStyle(workbook));
        }
        
        // Data
        for (InvoiceResponse invoice : invoices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(invoice.getInvoiceNumber());
            row.createCell(1).setCellValue(invoice.getInvoiceDate().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(invoice.getCustomer().getCompanyName());
            row.createCell(3).setCellValue(invoice.getStatus().toString());
            
            // Números de guía - concatenar todos los AWBs de los items
            String awbNumbers = "";
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                StringBuilder awbBuilder = new StringBuilder();
                for (InvoiceItemResponse item : invoice.getItems()) {
                    if (item.getAirWaybill() != null && item.getAirWaybill().getAwbNumber() != null) {
                        if (awbBuilder.length() > 0) {
                            awbBuilder.append(", ");
                        }
                        awbBuilder.append(item.getAirWaybill().getAwbNumber());
                    }
                }
                awbNumbers = awbBuilder.toString();
            }
            row.createCell(4).setCellValue(awbNumbers);
            
            row.createCell(5).setCellValue(invoice.getItems() != null ? invoice.getItems().size() : 0);
            
            Cell amountCell = row.createCell(6);
            amountCell.setCellValue(invoice.getTotalAmount().doubleValue());
            amountCell.setCellStyle(createCurrencyStyle(workbook));
            
            // Días transcurridos desde la fecha de la factura
            long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(invoice.getInvoiceDate(), LocalDate.now());
            row.createCell(7).setCellValue(daysElapsed);
            
            // Colorear según estado
            if (invoice.getStatus().toString().equals("PENDING")) {
                for (int i = 0; i < 8; i++) {
                    row.getCell(i).setCellStyle(createPendingStyle(workbook));
                }
            }
        }
        
        // Si se incluyen gráficos, añadir hoja de resumen visual
        if (includeCharts) {
            createChartsSheet(workbook, invoices, startDate, endDate);
        }
        
        return rowNum;
    }
    
    private void createChartsSheet(XSSFWorkbook workbook, List<InvoiceResponse> invoices, LocalDate startDate, LocalDate endDate) {
        Sheet chartSheet = workbook.createSheet("Gráficos y Análisis");
        int rowNum = 0;
        
        // Título
        Row titleRow = chartSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ANÁLISIS VISUAL DE FACTURAS");
        titleCell.setCellStyle(createHeaderStyle(workbook));
        chartSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));
        
        rowNum += 2;
        
        // Distribución por estado
        createSectionTitle(workbook, chartSheet, rowNum++, "Distribución por Estado");
        
        long paidCount = invoices.stream().filter(i -> i.getStatus().toString().equals("PAID")).count();
        long pendingCount = invoices.stream().filter(i -> i.getStatus().toString().equals("PENDING")).count();
        
        addKeyValueRow(workbook, chartSheet, rowNum++, "Pagadas:", paidCount);
        addKeyValueRow(workbook, chartSheet, rowNum++, "Pendientes:", pendingCount);
        
        rowNum += 2;
        
        // Top 5 clientes por monto
        createSectionTitle(workbook, chartSheet, rowNum++, "Top 5 Clientes por Monto");
        
        Map<String, BigDecimal> customerTotals = invoices.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    i -> i.getCustomer().getCompanyName(),
                    java.util.stream.Collectors.reducing(BigDecimal.ZERO, InvoiceResponse::getTotalAmount, BigDecimal::add)
                ));
        
        final int[] currentRow = {rowNum};
        customerTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    addKeyValueRow(workbook, chartSheet, currentRow[0], entry.getKey(), entry.getValue());
                    currentRow[0]++;
                });
        rowNum = currentRow[0];
    }
    
    private CellStyle createPendingStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
        
    public byte[] generateInvoiceExcel(InvoiceResponse invoice) throws ExportException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Factura " + invoice.getInvoiceNumber());
            
            int rowNum = createInvoiceSheet(workbook, sheet, invoice);
            
            autoSizeColumns(sheet, 5);
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
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));
        
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
        headerRow.createCell(1).setCellValue("N° Guía");
        headerRow.createCell(2).setCellValue("Descripción");
        headerRow.createCell(3).setCellValue("Precio Unitario");
        headerRow.createCell(4).setCellValue("Total");
        
        // Apply header style
        for (int i = 0; i < 5; i++) {
            headerRow.getCell(i).setCellStyle(createTableHeaderStyle(workbook));
        }
        
        // Items
        for (InvoiceItemResponse item : invoice.getItems()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("1");
            
            // Número de guía
            String awbNumber = "";
            if (item.getAirWaybill() != null && item.getAirWaybill().getAwbNumber() != null) {
                awbNumber = item.getAirWaybill().getAwbNumber();
            }
            row.createCell(1).setCellValue(awbNumber);
            
            row.createCell(2).setCellValue(item.getServiceDescription());
            
            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(item.getAmount().doubleValue());
            priceCell.setCellStyle(createCurrencyStyle(workbook));
            
            Cell totalCell = row.createCell(4);
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
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 4));
        
        return rowNum;
    }
    
    private void createTotalSection(XSSFWorkbook workbook, Sheet sheet, int rowNum, InvoiceResponse invoice) {
        Row row = sheet.createRow(rowNum);
        row.createCell(3).setCellValue("TOTAL:");
        
        Cell totalCell = row.createCell(4);
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
            case CUSTOMERS, INVOICING, FINANCIAL -> 3;
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
