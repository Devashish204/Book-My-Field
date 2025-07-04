package com.mit.project.views.new_booking;

import com.mit.project.dtos.BookingDetailsDto;
import com.mit.project.dtos.BookingRequestDto;
import com.mit.project.dtos.BookingResponseDto;
import com.mit.project.services.CourtConfigService;
import com.mit.project.services.booking.BookingService;
import com.mit.project.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("New Booking")
@Route(value = "/new-booking", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.PLUS_SOLID)
@RolesAllowed({"USER"})
@PermitAll
public class NewBookingView extends VerticalLayout {
  public static final String API_BASE_URL = "http://localhost:8080/api";

  @Autowired
  public NewBookingView(
      BookingService bookingService,
      CourtConfigService courtConfigService,
      RestTemplate restTemplate) {

    this.bookingService = bookingService;
    this.courtConfigService = courtConfigService;
    this.restTemplate = restTemplate;

    addClassName("dashboard-view");

    H1 logo = new H1("Create New Booking");
    logo.addClassName("logo");
    HorizontalLayout header;

    header = new HorizontalLayout(logo);

    add(header);

    setAlignItems(Alignment.CENTER);
    setSpacing(true);

    add(createForm());
  }

  private final BookingService bookingService;

  private final CourtConfigService courtConfigService;

  private final RestTemplate restTemplate;

  private final Grid<BookingDetailsDto> bookingGrid = new Grid<>(BookingDetailsDto.class);

  private ComboBox<String> sportField;
  private DatePicker dateField;
  private ComboBox<String> hourField;
  private Checkbox queueCheckbox;
  private Span availabilityLabel;
  private Button bookButton;
  private Button checkAvailabilityButton;

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private Map<String, Integer> hourMapping;

  private VerticalLayout createForm() {
    sportField = new ComboBox<>("Sport", courtConfigService.getCourtNames());

    dateField = new DatePicker("Booking Date");

    hourField = new ComboBox<>("Time Slot");
    hourMapping =
        IntStream.range(0, 24)
            .boxed()
            .collect(
                Collectors.toMap(
                    this::formatHourSlot, // Key: Displayed value
                    hour -> hour // Value: Internal value
                    ));

    hourField.setItems(hourMapping.keySet());
    hourField.setPlaceholder("Select Slot");
    hourField.setClearButtonVisible(true);
    hourField.addValueChangeListener(
        event -> {
          String selectedDisplayValue = event.getValue();
          Integer internalHour = hourMapping.get(selectedDisplayValue);
          System.out.println("Selected Hour (Internal): " + internalHour);
        });

    queueCheckbox = new Checkbox("Join the waiting queue if full");
    availabilityLabel = new Span("Available");
    availabilityLabel.getStyle().set("color", "green");
    availabilityLabel.setVisible(false);

    checkAvailabilityButton = new Button("Check Availability", event -> checkAvailability());
    bookButton = new Button("Book Court", event -> saveBooking());
    bookButton.setEnabled(false);

    return new VerticalLayout(
        sportField,
        dateField,
        hourField,
        checkAvailabilityButton,
        queueCheckbox,
        availabilityLabel,
        bookButton);
  }

  private void checkAvailability() {
    if (sportField.isEmpty() || dateField.isEmpty() || hourField.isEmpty()) {
      Notification.show("All fields are required!", 3000, Notification.Position.MIDDLE);
    }
    String sport = sportField.getValue();
    Date bookingDate = Date.valueOf(dateField.getValue());
    int bookingHour = parseHourFromSlot(hourField.getValue());

    try {
      boolean isAvailable =
          bookingService.getAvailableCourtId(sport, bookingDate, bookingHour).isPresent();

      if (isAvailable) {
        availabilityLabel.setText("Slot Available");
        availabilityLabel.getStyle().set("color", "green");
        availabilityLabel.setVisible(true);
        queueCheckbox.setVisible(false);
        bookButton.setEnabled(true);
      } else {
        availabilityLabel.setText("Slot Unavailable!");
        availabilityLabel.getStyle().set("color", "red");
        availabilityLabel.setVisible(true);
        queueCheckbox.setVisible(true);
        bookButton.setEnabled(queueCheckbox.getValue());
      }

      queueCheckbox.addValueChangeListener(
          event -> {
            if (!isAvailable) {
              bookButton.setEnabled(event.getValue());
            }
          });

    } catch (RuntimeException e) {
      Notification.show(
          "Error checking availability: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
    }
  }

  private void saveBooking() {
    if (sportField.isEmpty() || dateField.isEmpty() || hourField.isEmpty()) {
      Notification.show("All fields are required!", 3000, Notification.Position.MIDDLE);
      return;
    }
    BookingRequestDto requestDto = new BookingRequestDto();
    requestDto.setSport(sportField.getValue());
    requestDto.setBookingDate(Date.valueOf(dateField.getValue()));
    requestDto.setBookingHour(parseHourFromSlot(hourField.getValue()));
    requestDto.setPutMeInQueue(queueCheckbox.getValue());
    String userId = getCurrentUserId();
    requestDto.setUserId(userId);

    try {

      BookingResponseDto response =
          bookingService.createBooking(requestDto);


        if (response == null) {
            Notification.show("Booking failed: No response from server", 3000, Notification.Position.MIDDLE);
            return;
        }

        String message =
          response.isBookingConfirmed()
              ? "Booking Confirmed! Court: " + response.getCourtId()
              : "Added to Queue. Queue No: " + response.getQueueNumber();

      Notification.show(message, 3000, Notification.Position.MIDDLE);
    } catch (RuntimeException e) {
      Notification.show("Booking failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
    }

    sportField.clear();
    dateField.clear();
    hourField.clear();
    queueCheckbox.clear();
    // availabilityLabel.setVisible(false);
    bookButton.setEnabled(false);
  }

  private String getCurrentUserId() {
    // Example: Retrieve the userId from the Spring Security context
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  private String formatHourSlot(int hour) {
    int nextHour = (hour + 1) % 24;
    return formatHour(hour) + " - " + formatHour(nextHour);
  }

  private String formatHour(int hour) {
    int h = hour % 12 == 0 ? 12 : hour % 12;
    String period = hour < 12 ? "AM" : "PM";
    return h + " " + period;
  }

  private int parseHourFromSlot(String slot) {
    String firstPart = slot.split("-")[0].trim(); // e.g., "10 AM"
    String[] parts = firstPart.split(" ");
    int hour = Integer.parseInt(parts[0]);
    String ampm = parts[1];

    if (ampm.equalsIgnoreCase("AM")) {
      return hour == 12 ? 0 : hour;
    } else {
      return hour == 12 ? 12 : hour + 12;
    }
  }
}
