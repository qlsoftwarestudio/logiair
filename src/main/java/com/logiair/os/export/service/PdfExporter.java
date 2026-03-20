package com.logiair.os.export.service;

import com.logiair.os.dto.response.InvoiceResponse;
import com.logiair.os.dto.response.InvoiceItemResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Rectangle;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateInvoicePdf(InvoiceResponse invoice) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Document document = new Document(new Rectangle(595, 842));
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Título
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("FACTURA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Información de la factura
        Font infoFont = new Font(Font.HELVETICA, 12);
        
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{3f, 2f});
        
        // Número de factura
        infoTable.addCell(createCell("Número de Factura:", infoFont, Element.ALIGN_RIGHT, true));
        infoTable.addCell(createCell(invoice.getInvoiceNumber(), infoFont, Element.ALIGN_LEFT, false));
        
        // Fecha
        infoTable.addCell(createCell("Fecha:", infoFont, Element.ALIGN_RIGHT, true));
        infoTable.addCell(createCell(invoice.getInvoiceDate().format(DATE_FORMATTER), infoFont, Element.ALIGN_LEFT, false));
        
        // Estado
        infoTable.addCell(createCell("Estado:", infoFont, Element.ALIGN_RIGHT, true));
        infoTable.addCell(createCell(invoice.getStatus().toString(), infoFont, Element.ALIGN_LEFT, false));
        
        document.add(infoTable);
        
        // Espacio
        document.add(new Paragraph(" "));
        
        // Información del cliente
        Paragraph customerTitle = new Paragraph("Datos del Cliente", new Font(Font.HELVETICA, 14, Font.BOLD));
        customerTitle.setSpacingAfter(10);
        document.add(customerTitle);
        
        PdfPTable customerTable = new PdfPTable(2);
        customerTable.setWidthPercentage(100);
        customerTable.setWidths(new float[]{1f, 2f});
        
        customerTable.addCell(createCell("Cliente:", infoFont, Element.ALIGN_RIGHT, true));
        customerTable.addCell(createCell(invoice.getCustomer().getCompanyName(), infoFont, Element.ALIGN_LEFT, false));
        
        document.add(customerTable);
        
        // Espacio
        document.add(new Paragraph(" "));
        
        // Items de la factura
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            Paragraph itemsTitle = new Paragraph("Detalle de Servicios", new Font(Font.HELVETICA, 14, Font.BOLD));
            itemsTitle.setSpacingAfter(10);
            document.add(itemsTitle);
            
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{1f, 3f, 1f, 1f});
            
            // Encabezados
            itemsTable.addCell(createHeaderCell("Cant.", infoFont));
            itemsTable.addCell(createHeaderCell("Descripción", infoFont));
            itemsTable.addCell(createHeaderCell("Precio", infoFont));
            itemsTable.addCell(createHeaderCell("Total", infoFont));
            
            // Items
            for (InvoiceItemResponse item : invoice.getItems()) {
                itemsTable.addCell(createCell("1", infoFont, Element.ALIGN_CENTER, false));
                itemsTable.addCell(createCell(item.getServiceDescription(), infoFont, Element.ALIGN_LEFT, false));
                itemsTable.addCell(createCell("$" + item.getAmount().toString(), infoFont, Element.ALIGN_RIGHT, false));
                itemsTable.addCell(createCell("$" + item.getAmount().toString(), infoFont, Element.ALIGN_RIGHT, false));
            }
            
            document.add(itemsTable);
        }
        
        // Espacio
        document.add(new Paragraph(" "));
        
        // Total
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{3f, 1f});
        
        Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        totalTable.addCell(createCell("TOTAL:", totalFont, Element.ALIGN_RIGHT, true));
        totalTable.addCell(createCell("$" + invoice.getTotalAmount().toString(), totalFont, Element.ALIGN_RIGHT, false));
        
        document.add(totalTable);
        
        // Observaciones
        if (invoice.getObservations() != null && !invoice.getObservations().trim().isEmpty()) {
            document.add(new Paragraph(" "));
            Paragraph obsTitle = new Paragraph("Observaciones", new Font(Font.HELVETICA, 14, Font.BOLD));
            obsTitle.setSpacingAfter(10);
            document.add(obsTitle);
            
            Paragraph observations = new Paragraph(invoice.getObservations(), infoFont);
            document.add(observations);
        }
        
        document.close();
        
        return outputStream.toByteArray();
    }
    
    private PdfPCell createCell(String text, Font font, int alignment, boolean isBold) {
        Font cellFont = isBold ? new Font(font.getFamily(), font.getSize(), Font.BOLD) : font;
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
    
    private PdfPCell createHeaderCell(String text, Font font) {
        Font headerFont = new Font(font.getFamily(), font.getSize(), Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        cell.setBorder(Rectangle.BOX);
        return cell;
    }
}
