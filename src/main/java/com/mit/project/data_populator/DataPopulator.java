package com.mit.project.data_populator;

import com.mit.project.dtos.BookingRequestDto;
import com.mit.project.dtos.BookingResponseDto;
import com.mit.project.entities.Booking;
import com.mit.project.entities.CourtConfig;
import com.mit.project.repositories.BookingRepository;
import com.mit.project.services.CourtConfigService;
import com.mit.project.services.booking.BookingService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataPopulator implements CommandLineRunner {

  private final BookingService bookingService;
  private final BookingRepository bookingRepository;
  private final CourtConfigService courtConfigService;

  @Override
  public void run(String... args) throws Exception {

    createCourtConfig("Table Tennis", 4, false, 100);
    createCourtConfig("FootBall", 2, true, 200);
    createCourtConfig("Cricket", 5, true, 300);
    createCourtConfig("Badminton", 2, true, 150);

    BookingRequestDto booking1 = new BookingRequestDto();
    booking1.setUserId("devashish@gmail.com");
    booking1.setSport("BADMINTON");
    booking1.setBookingDate(Date.valueOf(LocalDate.now()));
    booking1.setBookingHour(16);
    bookingService.createBooking(booking1);

    BookingRequestDto booking2 = new BookingRequestDto();
    booking2.setUserId("devashish@gmail.com");
    booking2.setSport("BADMINTON");
    booking2.setBookingDate(Date.valueOf(LocalDate.now()));
    booking2.setBookingHour(16);
    BookingResponseDto createdBooking2 = bookingService.createBooking(booking2);

    Optional<Booking> bookins2FromDB =
        bookingRepository.findById(Long.valueOf(createdBooking2.getBookingId()));
    bookins2FromDB.get().setBookingConfirmed(false);
    bookingRepository.save(bookins2FromDB.get());

    BookingRequestDto booking3 = new BookingRequestDto();
    booking3.setUserId("devashish@gmail.com");
    booking3.setSport("BADMINTON");
    booking3.setBookingDate(Date.valueOf(LocalDate.now()));
    booking3.setBookingHour(16);
    bookingService.createBooking(booking3);

    BookingRequestDto booking4 = new BookingRequestDto();
    booking4.setUserId("devashish@gmail.com");
    booking4.setSport("BADMINTON");
    booking4.setBookingDate(Date.valueOf(LocalDate.now()));
    booking4.setBookingHour(16);
    booking4.setPutMeInQueue(true);
    bookingService.createBooking(booking4);

    BookingRequestDto booking5 = new BookingRequestDto();
    booking5.setUserId("devashish@gmail.com");
    booking5.setSport("BADMINTON");
    booking5.setBookingDate(Date.valueOf(LocalDate.now()));
    booking5.setBookingHour(16);
    booking5.setPutMeInQueue(true);
    bookingService.createBooking(booking5);

    bookingService.cancelBooking(3L);
  }

  private void createCourtConfig(
      String courtName, int total, boolean isAvailable, double hourlyRate) {
    CourtConfig courtConfig;
    courtConfig = new CourtConfig();
    courtConfig.setCourtName(courtName);
    courtConfig.setTotal(total);
    courtConfig.setAvailable(isAvailable);
    courtConfig.setHourlyRate(hourlyRate);
    courtConfigService.saveCourtConfig(courtConfig);
  }
}
