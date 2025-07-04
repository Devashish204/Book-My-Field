package com.mit.project.views.promotionalmail;

import com.mit.project.repositories.BookingRepository;
import com.mit.project.services.communication.CommunicationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Send Promotional Emails")
@Route(value = "admin/promotions")
@RolesAllowed("ADMIN")
@Component
public class PromotionalEmailView extends VerticalLayout {

    private final CommunicationService communicationService;
    private final BookingRepository bookingRepository;

    private final TextField subjectField = new TextField("Subject of mail");
    private final TextArea bodyField = new TextArea("Body");

    private final Button sendButton = new Button("Send");

    @Autowired
    public PromotionalEmailView(CommunicationService communicationService, BookingRepository bookingRepository) {
        this.communicationService = communicationService;
        this.bookingRepository = bookingRepository;

        setupForm();
        setupActions();
    }

    private void setupForm() {
        subjectField.setWidthFull();
        bodyField.setWidthFull();
        bodyField.setHeight("300px");

        sendButton.getStyle().set("margin-top", "15px");

        add(subjectField, bodyField, sendButton);
        setWidth("600px");
        setPadding(true);
        setSpacing(true);
    }

    private void setupActions() {
        sendButton.addClickListener(event -> {
            String subject = subjectField.getValue();
            String body = bodyField.getValue();

            if (subject.isEmpty() || body.isEmpty()) {
                Notification.show("Subject and Body cannot be empty.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                List<String> emails = bookingRepository.findDistinctEmails();

                for (String email : emails) {
                    communicationService.sendPromotionalEmail(email, subject, body);
                }

                Notification.show("Promotional emails sent successfully!", 4000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Failed to send promotional emails.", 4000, Notification.Position.MIDDLE);
            }
        });
    }
}
