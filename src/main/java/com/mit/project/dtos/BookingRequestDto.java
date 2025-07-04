package com.mit.project.dtos;

import java.sql.Date;
import lombok.Data;

@Data
public class BookingRequestDto {
  private String sport;
  private Date bookingDate;
  private Integer bookingHour;
  private boolean putMeInQueue;
  private String userId;
}
