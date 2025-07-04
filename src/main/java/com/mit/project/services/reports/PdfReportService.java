package com.mit.project.services.reports;

import com.mit.project.entities.Booking;
import com.mit.project.entities.Invoice;
import com.mit.project.repositories.BookingRepository;
import com.mit.project.repositories.InvoiceRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class PdfReportService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;

    public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Earnings Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 3, 2, 2, 2, 2});

            Stream.of("Court", "User", "Date", "Time", "Amount", "Status")
                    .forEach(header -> {
                        PdfPCell cell = new PdfPCell(new Phrase(header));
                        cell.setBackgroundColor(Color.LIGHT_GRAY);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);
                    });

            List<Booking> bookings = bookingRepository.findAll();
            List<Invoice> invoices = invoiceRepository.findAll();

            Map<Long, Double> invoiceMap = new HashMap<>();
            for (Invoice invoice : invoices) {
                invoiceMap.put(invoice.getBookingId(), invoice.getAmount());
            }

            double totalAmount = 0;

            for (Booking booking : bookings) {
                LocalDate bookingDate = booking.getBookingDate().toLocalDate();

                if (!bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate)) {
                    boolean isCancelled = booking.isCancelled();
                    String status = isCancelled ? "Cancelled" : (booking.isBookingConfirmed() ? "Confirmed" : "In Queue");

                    double amount = invoiceMap.getOrDefault(booking.getBookingId(), 0.0);

                    table.addCell(booking.getCourtId() != null ? booking.getCourtId() : "");
                    table.addCell(booking.getUserId());
                    table.addCell(booking.getBookingDate().toString());
                    table.addCell(booking.getBookingHour() + ":00");
                    table.addCell(String.valueOf(isCancelled ? 0.0 : amount));
                    table.addCell(status);

                    if (!isCancelled) {
                        totalAmount += amount;
                    }
                }
            }

            document.add(table);

            document.add(new Paragraph(" "));
            Paragraph summary = new Paragraph("Total Collected: ₹" + totalAmount, new Font(Font.HELVETICA, 14, Font.BOLD));
            summary.setAlignment(Element.ALIGN_RIGHT);
            document.add(summary);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
