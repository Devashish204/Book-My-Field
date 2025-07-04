package com.mit.project.dtos;

import java.sql.Date;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Data
public class BookingResponseDto {
  private Long bookingId;
  private String sport;
  private Date date;
  private int hour;
  private String courtId;
  private boolean isBookingConfirmed;
  private int queueNumber;
}
