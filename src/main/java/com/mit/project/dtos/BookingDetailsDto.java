package com.mit.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDetailsDto {
    private String sport;
    private LocalDate bookingDate;
    private Integer bookingHour;
    private boolean isInQueue;
    private String reservationist;
    private Long bookingId;
    private String courtId;
    private boolean isCancelled;
}
