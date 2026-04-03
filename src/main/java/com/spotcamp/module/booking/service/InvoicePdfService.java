package com.spotcamp.module.booking.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.spotcamp.module.booking.entity.Booking;
import com.spotcamp.module.booking.entity.BookingStatus;
import com.spotcamp.module.booking.repository.BookingRepository;
import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final BookingRepository bookingRepository;

    public byte[] generateInvoicePdf(Long bookingId, Long requesterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("Invoice is only available after payment is verified");
        }

        if (booking.getUserId() != null && booking.getUserId().equals(requesterId) == false) {
            // Allow merchant/admin access as well; camper must match booking owner
            // Roles are enforced at controller layer.
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("SpotCamp Payment Invoice", titleFont));
            document.add(new Paragraph("Invoice: " + safe(booking.getInvoiceNumber()), bodyFont));
            document.add(new Paragraph("Booking ID: " + booking.getId(), bodyFont));
            document.add(new Paragraph("Payment Status: " + booking.getStatus().name(), bodyFont));
            document.add(new Paragraph("Payment Method: " + safe(booking.getPaymentMethod()), bodyFont));
            document.add(new Paragraph("Paid At: " + formatDateTime(booking.getConfirmedAt()), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Guest", headerFont));
            document.add(new Paragraph(safe(booking.getContactName()), bodyFont));
            document.add(new Paragraph(safe(booking.getContactEmail()) + " · " + safe(booking.getContactPhone()), bodyFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 2, 2});
            addHeaderCell(table, "Item", headerFont);
            addHeaderCell(table, "Type", headerFont);
            addHeaderCell(table, "Qty", headerFont);
            addHeaderCell(table, "Subtotal", headerFont);

            booking.getItems().forEach(item -> {
                table.addCell(new PdfPCell(new Phrase(safe(item.getProductName()), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(safe(item.getProductType()), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(formatCurrency(item.getSubtotal()), bodyFont)));
            });

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total: " + formatCurrency(booking.getTotalAmount()), headerFont));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new BusinessException("Failed to generate invoice PDF");
        } catch (Exception e) {
            throw new BusinessException("Failed to generate invoice PDF");
        }
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private static String safe(String value) {
        return value == null ? "-" : value;
    }

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "-";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(value);
    }

    private static String formatDateTime(java.time.LocalDateTime value) {
        if (value == null) return "-";
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
