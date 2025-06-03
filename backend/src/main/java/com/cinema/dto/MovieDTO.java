package com.cinema.dto;

import com.cinema.model.MovieStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private String director;
    private String cast;
    private Integer duration;
    private String genre;
    private String language;
    private String rating;
    private String trailerUrl;
    private String posterUrl;
    private LocalDateTime releaseDate;
    private MovieStatus status;
    private String statusDisplayName;
}