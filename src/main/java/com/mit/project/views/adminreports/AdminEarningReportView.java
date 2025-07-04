package com.mit.project.views.adminreports;

import com.mit.project.services.reports.PdfReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.UUID;

@Route("admin/earning-report")
@RolesAllowed("ADMIN")
public class AdminEarningReportView extends VerticalLayout {

    private final PdfReportService pdfReportService;
    private final DatePicker startDatePicker = new DatePicker("Start Date");
    private final DatePicker endDatePicker = new DatePicker("End Date");
    private final Anchor downloadLink = new Anchor();
    private final Button generateAndDownloadButton = new Button("Download PDF Report");

    public AdminEarningReportView(PdfReportService pdfReportService) {
        this.pdfReportService = pdfReportService;

        setPadding(true);
        add(new H2("Earning Report"));

        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(generateAndDownloadButton);

        generateAndDownloadButton.addClickListener(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            byte[] pdfData = pdfReportService.generatePdfReport(startDate, endDate);
            String fileName = "earning-report-" + UUID.randomUUID() + ".pdf";

            StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(pdfData));
            downloadLink.setHref(resource);

            downloadLink.getElement().callJsFunction("click");
        });

        add(startDatePicker, endDatePicker, downloadLink);
    }
}
