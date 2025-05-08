package com.cinema.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long screeningId;
    private String movieTitle;
    private LocalDateTime screeningTime;
    private String hallNumber;
    private String seatNumber;
    private LocalDateTime bookingTime;
    private Double price;
    private String status;
    private String qrCode;
}