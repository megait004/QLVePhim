package com.cinema.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer duration;
    private LocalDate releaseDate;
    private String director;
    private String movieCast;
    private String posterUrl;
    private Boolean isShowing;
}