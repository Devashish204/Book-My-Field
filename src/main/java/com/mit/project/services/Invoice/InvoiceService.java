package com.mit.project.services.Invoice;

import com.mit.project.entities.Invoice;
import com.mit.project.repositories.InvoiceRepository;
import com.mit.project.services.communication.CommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.sql.Date;

@Service
@RequiredArgsConstructor
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final CommunicationService communicationService;

  public Invoice generateInvoice(
      Long bookingId, double amount, Date generationDate, String email, String name) {
    Invoice invoice = new Invoice();
    invoice.setBookingId(bookingId);
    invoice.setAmount(amount);
    invoice.setGenerationDate(generationDate);

    Invoice savedInvoice = invoiceRepository.save(invoice);

    Context context = new Context();
    context.setVariable("customerName", name);
    context.setVariable("bookingId", bookingId);
    context.setVariable("amount", amount);
    context.setVariable("generationDate", generationDate.toString());
    context.setVariable("title", "Invoice Details");
    context.setVariable("subject", "Your Invoice");

    communicationService.sendInvoice(context, email);

    return savedInvoice;
  }
}
