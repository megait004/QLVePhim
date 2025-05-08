package com.cinema.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MovieSearchRequest {
    private String title;
    private String genre;
    private Boolean isShowing;
    private LocalDate fromDate;
    private LocalDate toDate;
}