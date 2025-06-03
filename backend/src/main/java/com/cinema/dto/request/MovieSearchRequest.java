package com.cinema.dto.request;

import com.cinema.model.MovieStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MovieSearchRequest {
    private String title;
    private String genre;
    private MovieStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
}