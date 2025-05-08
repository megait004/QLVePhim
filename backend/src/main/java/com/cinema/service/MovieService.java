package com.cinema.service;

import com.cinema.dto.MovieDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.model.Movie;
import com.cinema.model.Screening;
import com.cinema.model.Ticket;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private TicketRepository ticketRepository;

    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = convertToEntity(movieDTO);
        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        movieDTO.setId(id); // Ensure the ID is set correctly
        Movie movie = convertToEntity(movieDTO);
        Movie updatedMovie = movieRepository.save(movie);
        return convertToDTO(updatedMovie);
    }

    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MovieDTO> getNowShowing() {
        return searchMovies(null, null, true, null, null);
    }

    public List<MovieDTO> getMoviesByGenre(String genre) {
        return searchMovies(null, genre, null, null, null);
    }

    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return convertToDTO(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        // Get all screenings for this movie
        List<Screening> screenings = screeningRepository.findByMovieId(id);

        // For each screening
        for (Screening screening : screenings) {
            // Delete all tickets for this screening
            ticketRepository.deleteByScreeningId(screening.getId());
        }

        // Delete all screenings
        screeningRepository.deleteByMovieId(id);

        // Finally delete the movie
        movieRepository.delete(movie);
    }

    public List<MovieDTO> searchMovies(String title, String genre, Boolean isShowing, LocalDate fromDate, LocalDate toDate) {
        List<Movie> results = movieRepository.searchMovies(title, genre, isShowing, fromDate, toDate);
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setGenre(movie.getGenre());
        dto.setDuration(movie.getDuration());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setDirector(movie.getDirector());
        dto.setMovieCast(movie.getMovieCast());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setIsShowing(movie.getIsShowing());
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        if (dto.getId() != null) {
            movie.setId(dto.getId());
        }
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setGenre(dto.getGenre());
        movie.setDuration(dto.getDuration());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setDirector(dto.getDirector());
        movie.setMovieCast(dto.getMovieCast());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setIsShowing(dto.getIsShowing());
        return movie;
    }
}