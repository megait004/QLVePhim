package com.cinema.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScreeningDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private LocalDateTime startTime;
    private String hallNumber;
    private Double price;
    private Integer availableSeats;
}