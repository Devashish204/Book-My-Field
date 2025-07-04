package com.mit.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.sql.Date;

import lombok.Data;

@Entity
@Table(name = "booking")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @NotBlank(message = "User id is required for booking")
    @Column(name = "user_id", nullable = false)
    private String userId;

    private Date bookingDate;

    @Min(value = 0, message = "Booking hour should be between 0 hrs and 23 hrs")
    @Max(value = 23, message = "Booking cannot be beyond 23 hrs")
    private Integer bookingHour;

    private int queueNumber;

    private String courtId;

    private String sport;

    private boolean isBookingConfirmed;

    private boolean isCancelled;


}
