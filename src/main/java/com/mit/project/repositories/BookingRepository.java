package com.mit.project.repositories;

import com.mit.project.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM booking WHERE sport = :sport AND booking_date = :date AND booking_hour = :hour AND is_booking_confirmed = true")
    List<Booking> findBookingForSportDateHour(@Param("sport") String sport, @Param("date") Date date, @Param("hour") int hour);

    @Query(nativeQuery = true, value = "SELECT max(queue_number) FROM booking WHERE sport= :sport AND booking_date = :date AND booking_hour= :hour AND is_booking_confirmed = false")
    Optional<Integer> findMaxQueueNumber(@Param("sport") String sport, @Param("date") Date date, @Param("hour") int hour);

    @Query(nativeQuery = true, value = "SELECT * FROM booking WHERE booking_date = :date ORDER BY booking_hour ASC LIMIT 100")
    List<Booking> findBookingsForDay(@Param("date") Date date);

    List<Booking> findByUserIdOrderByBookingDateDesc(String userId);

    List<Booking> findAllBySportAndBookingDateAndBookingHourAndIsCancelledFalseAndIsBookingConfirmedFalseOrderByQueueNumberAsc(String sport, Date bookingDate, Integer bookingHour);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findAllBookingsBetweenDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT DISTINCT b.userId FROM Booking b WHERE b.userId IS NOT NULL")
    List<String> findDistinctEmails();


}
