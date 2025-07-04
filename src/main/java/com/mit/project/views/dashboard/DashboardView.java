package com.mit.project.views.dashboard;

import com.mit.project.SecurityService;
import com.mit.project.dtos.BookingDetailsDto;
import com.mit.project.services.booking.BookingService;
import com.mit.project.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Dashboard")
@Route(value = "/user-dashboard", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.CHART_AREA_SOLID)
@RolesAllowed({"USER"})
public class DashboardView extends Main {

  private final BookingService bookingService;
  private final Grid<BookingDetailsDto> bookingGrid = new Grid<>(BookingDetailsDto.class);
  private List<BookingDetailsDto> allBookings;

  public DashboardView(
      @Autowired SecurityService securityService, @Autowired BookingService bookingService) {
    this.bookingService = bookingService;

    addClassName("dashboard-view");

    H1 logo = new H1("Welcome, " + securityService.getAuthenticatedUser().getUsername());
    logo.addClassName("logo");

    VerticalLayout layout = new VerticalLayout();
    layout.add(logo);

    configureBookingGrid();
    allBookings = bookingService.getBookingsForUser();
    bookingGrid.setItems(allBookings);

    layout.add(getSortControls(), bookingGrid);
    add(layout);
  }

  private void configureBookingGrid() {
    bookingGrid.addClassName("booking-grid");
    bookingGrid.setSizeFull();
    bookingGrid.setAllRowsVisible(true);
    bookingGrid.removeAllColumns();

    bookingGrid.addColumn(BookingDetailsDto::getBookingId).setHeader("Booking ID");
    bookingGrid.addColumn(BookingDetailsDto::getSport).setHeader("Sport");
    bookingGrid.addColumn(BookingDetailsDto::getBookingDate).setHeader("Booking Date");
    bookingGrid.addColumn(BookingDetailsDto::getBookingHour).setHeader("Hour");
    bookingGrid.addColumn(BookingDetailsDto::isInQueue).setHeader("In Queue");
    bookingGrid.addColumn(BookingDetailsDto::getCourtId).setHeader("Court ID");

    bookingGrid
        .addColumn(
            new ComponentRenderer<>(
                booking -> {
                  Button cancelButton =
                      new Button(
                          "Cancel",
                          e -> {
                            try {
                              bookingService.cancelBooking(booking.getBookingId());

                              Notification.show("Cancelled booking ID: " + booking.getBookingId());
                              allBookings = bookingService.getBookingsForUser();
                              bookingGrid.setItems(allBookings);
                            } catch (Exception ex) {
                              Notification.show(
                                  "Failed to cancel booking: " + ex.getMessage(),
                                  5000,
                                  Notification.Position.MIDDLE);
                            }
                          });

                  cancelButton.getStyle().set("color", "white");
                  cancelButton.getStyle().set("background-color", "red");

                  LocalDateTime now = LocalDateTime.now();
                  LocalDateTime bookingDateTime =
                      LocalDateTime.of(
                          booking.getBookingDate(), LocalTime.of(booking.getBookingHour(), 0));
                  Duration timeUntilBooking = Duration.between(now, bookingDateTime);

                  if (booking.isCancelled()) {
                    cancelButton.setEnabled(false);
                    cancelButton.getStyle().set("color", "red");
                    cancelButton.getStyle().set("background-color", "white");
                    cancelButton.setText("Cancelled");
                  } else if (timeUntilBooking.toMinutes() < 15) {
                    cancelButton.setEnabled(false);
                    cancelButton.setText("Too Late to Cancel");
                  }

                  return cancelButton;
                }))
        .setHeader("Action");
  }

  private HorizontalLayout getSortControls() {
    ComboBox<String> sortBox = new ComboBox<>();
    sortBox.setLabel("Sort by Time");
    sortBox.setItems("Ascending", "Descending");

    sortBox.addValueChangeListener(
        event -> {
          String selected = event.getValue();
          if (selected != null) {
            List<BookingDetailsDto> sorted =
                allBookings.stream()
                    .sorted(
                        (b1, b2) -> {
                          LocalDateTime dt1 =
                              LocalDateTime.of(
                                  b1.getBookingDate(), LocalTime.of(b1.getBookingHour(), 0));
                          LocalDateTime dt2 =
                              LocalDateTime.of(
                                  b2.getBookingDate(), LocalTime.of(b2.getBookingHour(), 0));
                          return selected.equals("Ascending")
                              ? dt1.compareTo(dt2)
                              : dt2.compareTo(dt1);
                        })
                    .collect(Collectors.toList());

            bookingGrid.setItems(sorted);
          }
        });

    return new HorizontalLayout(sortBox);
  }
}
