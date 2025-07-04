package com.mit.project.restcontroller;

import com.mit.project.dtos.BookingDetailsDto;
import com.mit.project.dtos.BookingRequestDto;
import com.mit.project.dtos.BookingResponseDto;
import com.mit.project.services.booking.BookingService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingRestController {

  @Autowired private BookingService bookingService;

  @GetMapping
  public List<BookingDetailsDto> getBookingsForDay(
      @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")
          LocalDate localDate) {

    localDate = (localDate != null) ? localDate : LocalDate.now();
    Date sqlDate = Date.valueOf(localDate);
    return bookingService.getBookingsForDay(sqlDate);
  }

  @PostMapping("/new-booking")
  public BookingResponseDto createBooking(@RequestBody BookingRequestDto bookingDetailsDto) {
    log.info(String.valueOf(bookingDetailsDto));
    return bookingService.createBooking(bookingDetailsDto);
  }

  @PutMapping("/cancel/{bookingId}")
  public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
    log.info("Cancelling booking with ID: {}", bookingId);
    BookingResponseDto cancelledBooking = bookingService.cancelBooking(bookingId);
    if (cancelledBooking == null) {
      log.warn("Booking not found or already cancelled for ID: {}", bookingId);
      return ResponseEntity.status(404).body("Booking not found or already cancelled.");
    }
    log.info("Booking cancelled successfully for ID: {}", bookingId);
    return ResponseEntity.ok(cancelledBooking);
  }
}
