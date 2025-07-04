package com.mit.project.services.communication;

import com.mit.project.services.otp.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
public class CommunicationService {
    @Autowired
    public EmailService emailService;

    @Async
    public void sendBookingConfrimation(String toEmail, String customerName, String bookingId, String venue) {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("date", venue);
        context.setVariable("title", "Booking Confirmed");
        emailService.sendMail(context, "confirmation", toEmail, "Your Booking Confirmed");
    }

    public void sendBookingCancellation(String toEmail, String customerName, String bookingId, String venue) {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("date", venue + "Hrs");
        emailService.sendMail(context, "Cancellation", toEmail, "Your Booking Cancelled");
    }

    public void sendInvoice(Context context, String toEmail) {
        emailService.sendMail(context, "Invoice", toEmail, "Your Invoice");
    }

    public void sendInQueueNotification(String toEmail, String customerName, String bookingId, String venue) {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("date", venue);
        context.setVariable("title", "You're in Queue");
        context.setVariable("subject", "Booking In Queue");
        emailService.sendMail(context, "inqueue", toEmail, "You're in Queue");
    }

    @Async
    public void sendPromotionalEmail(String toEmail, String subject, String body) {
        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("body", body);
        emailService.sendMail(context, "promotional-mail", toEmail, subject);
    }

}
