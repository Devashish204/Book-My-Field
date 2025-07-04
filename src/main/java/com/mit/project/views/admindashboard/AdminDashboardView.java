package com.mit.project.views.admindashboard;

import com.mit.project.dtos.BookingDetailsDto;
import com.mit.project.services.booking.BookingService;
import com.mit.project.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@PageTitle("Admin Dashboard")
@Route(value = "admin-dashboard", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final BookingService bookingService;
    private final Grid<BookingDetailsDto> bookingGrid = new Grid<>(BookingDetailsDto.class, false);
    private final DatePicker datePicker = new DatePicker("Select Date");

    public AdminDashboardView(BookingService bookingService) {
        this.bookingService = bookingService;
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Admin Dashboard");
        add(title);

        HorizontalLayout cardLayout = new HorizontalLayout();
        cardLayout.setSpacing(true);

        List<BookingDetailsDto> allBookingsToday = getTodaysBookings();
        List<BookingDetailsDto> allBookings = bookingService.getBookingsForAllUser();

        int todayCount = allBookingsToday.size();
        int monthlyCount = getMonthlyBookingCount(allBookings);

        cardLayout.add(createCard("Today's Total Bookings", todayCount));
        cardLayout.add(createCard("Monthly Total Bookings", monthlyCount));
        add(cardLayout);

        datePicker.setValue(LocalDate.now());
        datePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            if (selectedDate != null) {
                List<BookingDetailsDto> bookings = bookingService.getBookingsForDay(Date.valueOf(selectedDate));
                bookingGrid.setItems(bookings);
            }
        });
        add(datePicker);

        add(new H2("Bookings"));

        configureGrid();
        bookingGrid.setItems(allBookingsToday);
        add(bookingGrid);
    }

    private List<BookingDetailsDto> getTodaysBookings() {
        return bookingService.getBookingsForDay(Date.valueOf(LocalDate.now()));
    }

    private int getMonthlyBookingCount(List<BookingDetailsDto> allBookings) {
        YearMonth currentMonth = YearMonth.now();
        return (int) allBookings.stream().filter(b -> {
            LocalDate bookingDate = b.getBookingDate();
            return bookingDate != null && bookingDate.getMonth() == currentMonth.getMonth() && bookingDate.getYear() == currentMonth.getYear();
        }).count();
    }

    private Div createCard(String title, int value) {
        Div card = new Div();
        card.getStyle().set("padding", "1em");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("background-color", "var(--lumo-base-color)");
        card.getStyle().set("width", "250px");

        H2 header = new H2(title);
        header.getStyle().set("font-size", "1.1em");

        Span count = new Span(String.valueOf(value));
        count.getStyle().set("font-size", "2em");
        count.getStyle().set("font-weight", "bold");

        card.add(header, count);
        return card;
    }

    private void configureGrid() {
        bookingGrid.addColumn(BookingDetailsDto::getBookingId).setHeader("Booking ID");
        bookingGrid.addColumn(BookingDetailsDto::getSport).setHeader("Sport");
        bookingGrid.addColumn(BookingDetailsDto::getCourtId).setHeader("Court ID");
        bookingGrid.addColumn(BookingDetailsDto::getBookingDate).setHeader("Date");
        bookingGrid.addColumn(BookingDetailsDto::getBookingHour).setHeader("Hour");
        bookingGrid.addColumn(BookingDetailsDto::getReservationist).setHeader("User");
        bookingGrid.addColumn(b -> b.isInQueue() ? "Yes" : "No").setHeader("In Queue");

        bookingGrid.addComponentColumn(booking -> {
            Button cancelButton = new Button("Cancel");
            if (booking.isCancelled()) {
                cancelButton.setText("Cancelled");
                cancelButton.setEnabled(false);
            } else {
                cancelButton.addClickListener(e -> openCancelDialog(booking, cancelButton));
            }
            return cancelButton;
        }).setHeader("Actions");
    }

    private void openCancelDialog(BookingDetailsDto booking, Button cancelButton) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cancel Booking");

        TextArea reasonField = new TextArea("Reason for cancellation");
        reasonField.setWidthFull();

        Button confirmBtn = new Button("Confirm", e -> {
            if (reasonField.getValue().isEmpty()) {
                Notification.show("Please enter a reason");
                return;
            }

            bookingService.cancelBooking(booking.getBookingId());
            booking.setCancelled(true);
            cancelButton.setText("Cancelled");
            cancelButton.setEnabled(false);

            Notification.show("Booking cancelled with reason: " + reasonField.getValue());
            dialog.close();
        });

        Button cancelBtn = new Button("Dismiss", e -> dialog.close());

        dialog.add(new FormLayout(reasonField));
        dialog.getFooter().add(confirmBtn, cancelBtn);
        dialog.open();
    }
}
