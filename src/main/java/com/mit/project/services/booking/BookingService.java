package com.mit.project.services.booking;

import com.mit.project.dtos.BookingDetailsDto;
import com.mit.project.dtos.BookingRequestDto;
import com.mit.project.dtos.BookingResponseDto;
import com.mit.project.entities.Booking;
import com.mit.project.entities.CourtConfig;
import com.mit.project.repositories.BookingRepository;
import com.mit.project.repositories.CourtConfigRepository;
import com.mit.project.services.Invoice.InvoiceService;
import com.mit.project.services.communication.CommunicationService;
import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

  private final BookingRepository bookingRepository;
  private final CourtConfigRepository courtConfigRepository;
  private final CommunicationService communicationService;
  private final InvoiceService invoiceService;

  public Optional<String> getAvailableCourtId(String sport, Date date, int hour) {
    Optional<CourtConfig> courtConfig = courtConfigRepository.findById(sport.toUpperCase());

    if (courtConfig.isEmpty() || !courtConfig.get().isAvailable()) {
      throw new RuntimeException("Court not found or not available");
    }

    List<Booking> confirmedBookings =
        bookingRepository.findBookingForSportDateHour(sport, date, hour);

    if (courtConfig.get().getTotal() > confirmedBookings.size()) {
      for (int i = 1; i <= courtConfig.get().getTotal(); i++) {
        if (confirmedBookings.size() == 0) {
          return Optional.of(sport.toUpperCase() + "-1");
        }
        String courtId = sport.toUpperCase() + "-" + i;
        for (int j = 0; j < confirmedBookings.size(); j++) {
          if (confirmedBookings.get(j).getCourtId().equals(courtId)) {
            break;
          } else {
            return Optional.of(courtId);
          }
        }
      }
    }
    return Optional.empty();
  }

  public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto) {
    Optional<String> availableCourtId =
        getAvailableCourtId(
            bookingRequestDto.getSport(),
            bookingRequestDto.getBookingDate(),
            bookingRequestDto.getBookingHour());
    if (availableCourtId.isPresent()) {

      Booking booking = dtoToEntity(bookingRequestDto);
      booking.setCourtId(availableCourtId.get());
      booking.setBookingConfirmed(true);
      booking = bookingRepository.save(booking);

      if (booking.isBookingConfirmed()) {
        Optional<CourtConfig> courtConfigOptional =
            courtConfigRepository.findById(booking.getSport().toUpperCase());
        if (courtConfigOptional.isPresent()) {
          double hourlyCharge = courtConfigOptional.get().getHourlyRate();
          double totalCharge = hourlyCharge * 1;
          Date generationDate = Date.valueOf(LocalDate.now());
          String email = booking.getUserId();
          String name = email.substring(0, email.indexOf("@"));
          invoiceService.generateInvoice(
              booking.getBookingId(), totalCharge, generationDate, email, name);
        }
      }
      communicationService.sendBookingConfrimation(
          booking.getUserId(),
          booking.getUserId().substring(0, booking.getUserId().indexOf("@")),
          String.valueOf(booking.getBookingId()),
          booking.getBookingDate().toString() + " " + booking.getBookingHour());
      return enityToResponseDto(booking);
    } else if (bookingRequestDto.isPutMeInQueue()) {
      Booking booking = dtoToEntity(bookingRequestDto);

      Optional<Integer> maxQueueNumber =
          bookingRepository.findMaxQueueNumber(
              booking.getSport(), booking.getBookingDate(), booking.getBookingHour());
      if (maxQueueNumber.isEmpty()) {
        booking.setQueueNumber(1);
      } else {
        booking.setQueueNumber(maxQueueNumber.get() + 1);
      }
      booking.setBookingConfirmed(false);
      booking = bookingRepository.save(booking);
      communicationService.sendInQueueNotification(
          booking.getUserId(),
          booking.getUserId().substring(0, booking.getUserId().indexOf("@")),
          String.valueOf(booking.getBookingId()),
          booking.getBookingDate().toString() + " " + booking.getBookingHour());
      return enityToResponseDto(booking);
    }
    throw new RuntimeException("Court not available");
  }

  public Booking dtoToEntity(BookingRequestDto bookingRequestDto) {
    Booking booking = new Booking();
    booking.setUserId(bookingRequestDto.getUserId());
    booking.setSport(bookingRequestDto.getSport());
    booking.setBookingDate(bookingRequestDto.getBookingDate());
    booking.setBookingHour(bookingRequestDto.getBookingHour());

    return booking;
  }

  public BookingResponseDto enityToResponseDto(Booking booking) {
    BookingResponseDto bookingResponseDto = new BookingResponseDto();
    bookingResponseDto.setBookingId(Long.valueOf(booking.getBookingId()));
    bookingResponseDto.setSport(booking.getSport());
    bookingResponseDto.setDate(booking.getBookingDate());
    bookingResponseDto.setHour(booking.getBookingHour());
    bookingResponseDto.setCourtId(booking.getCourtId());
    bookingResponseDto.setBookingConfirmed(booking.isBookingConfirmed());
    bookingResponseDto.setQueueNumber(booking.getQueueNumber());

    return bookingResponseDto;
  }

  public List<BookingDetailsDto> getBookingsForDay(Date date) {
    if (date == null) {
      LocalDate localDate = LocalDate.now();
      date = Date.valueOf(localDate);
    }

    List<Booking> bookings = bookingRepository.findBookingsForDay(date);

    return bookings.stream().map(this::toBookingDetailsDto).collect(Collectors.toList());
  }

  public List<BookingDetailsDto> getBookingsForUser() {

    List<Booking> bookings =
        bookingRepository.findByUserIdOrderByBookingDateDesc(
            SecurityContextHolder.getContext().getAuthentication().getName());

    return bookings.stream().map(this::toBookingDetailsDto).collect(Collectors.toList());
  }

  private BookingDetailsDto toBookingDetailsDto(Booking booking) {
    return BookingDetailsDto.builder()
        .bookingDate(booking.getBookingDate().toLocalDate())
        .bookingId(booking.getBookingId())
        .bookingHour(booking.getBookingHour())
        .isInQueue(!booking.isBookingConfirmed() && !booking.isCancelled())
        .isCancelled(booking.isCancelled())
        .reservationist(booking.getUserId())
        .sport(booking.getSport())
        .courtId(booking.getCourtId())
        .build();
  }

  public BookingResponseDto cancelBooking(Long bookingId) {
    Booking canclledBooking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

    if (canclledBooking.isCancelled()) {
      throw new RuntimeException("Booking already cancelled");
    }

    canclledBooking.setCancelled(true);
    canclledBooking.setBookingConfirmed(false);
    String freedCourtID = canclledBooking.getCourtId();
    canclledBooking.setCourtId(null);

    bookingRepository.save(canclledBooking);
    communicationService.sendBookingCancellation(
        canclledBooking.getUserId(),
        canclledBooking.getUserId().substring(0, canclledBooking.getUserId().indexOf("@")),
        String.valueOf(canclledBooking.getBookingId()),
        canclledBooking.getBookingDate().toString() + " " + canclledBooking.getBookingHour());

    List<Booking> queueList =
        bookingRepository
            .findAllBySportAndBookingDateAndBookingHourAndIsCancelledFalseAndIsBookingConfirmedFalseOrderByQueueNumberAsc(
                canclledBooking.getSport(),
                canclledBooking.getBookingDate(),
                canclledBooking.getBookingHour());

    if (!queueList.isEmpty() && freedCourtID != null) {
      Booking nextInQueue = queueList.get(0);
      nextInQueue.setBookingConfirmed(true);
      nextInQueue.setQueueNumber(0);
      nextInQueue.setCourtId(freedCourtID);
      bookingRepository.save(nextInQueue);

      try{
      communicationService.sendBookingConfrimation(
          nextInQueue.getUserId(),
          nextInQueue.getUserId().substring(0, nextInQueue.getUserId().indexOf("@")),
          String.valueOf(nextInQueue.getBookingId()),
          nextInQueue.getBookingDate().toString() + " " + nextInQueue.getBookingHour());
          log.info("Booking confirmation email sent to: " + nextInQueue.getUserId());
      }catch (Exception e){
        log.error("Failed to send confirmation email for booking ID: " + nextInQueue.getBookingId(), e);
      }
      queueList.remove(0);
      bookingRepository.saveAll(queueList);
    }

    for (int i = 0; i < queueList.size(); i++) {
      Booking b = queueList.get(i);
      b.setQueueNumber(i + 1);
    }
    bookingRepository.saveAll(queueList);

    return enityToResponseDto(canclledBooking);
  }

  public List<BookingDetailsDto> getBookingsForAllUser() {
    List<Booking> bookings = bookingRepository.findAll();
    return bookings.stream().map(this::toBookingDetailsDto).collect(Collectors.toList());
  }
}
