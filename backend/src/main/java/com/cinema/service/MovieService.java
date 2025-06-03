package com.cinema.service;

import com.cinema.dto.MovieDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.model.Movie;
import com.cinema.model.MovieStatus;
import com.cinema.model.Screening;
import com.cinema.model.Ticket;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
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

    @Autowired
    private FileUploadService fileUploadService;

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

    public List<MovieDTO> getNowShowingMovies() {
        return movieRepository.findByStatus(MovieStatus.NOW_SHOWING).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MovieDTO> getComingSoonMovies() {
        return movieRepository.findByStatus(MovieStatus.COMING_SOON).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MovieDTO> getHotMovies() {
        return movieRepository.findByStatus(MovieStatus.HOT).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MovieDTO> getMoviesByGenre(String genre) {
        return movieRepository.findByGenreAndStatus(genre, MovieStatus.NOW_SHOWING).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    public List<MovieDTO> searchMovies(String title, String genre, MovieStatus status, LocalDate fromDate, LocalDate toDate) {
        List<Movie> results = movieRepository.searchMovies(
            title,
            genre,
            status != null ? status.name() : null,
            fromDate,
            toDate
        );
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO updateMoviePoster(Long id, MultipartFile posterFile) throws IOException {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        String posterUrl = fileUploadService.uploadMoviePoster(posterFile);
        movie.setPosterUrl(posterUrl);
        Movie updatedMovie = movieRepository.save(movie);
        return convertToDTO(updatedMovie);
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDirector(movie.getDirector());
        dto.setCast(movie.getCast());
        dto.setDuration(movie.getDuration());
        dto.setGenre(movie.getGenre());
        dto.setLanguage(movie.getLanguage());
        dto.setRating(movie.getRating());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setStatus(movie.getStatus());
        dto.setStatusDisplayName(movie.getStatus().getDisplayName());
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        movie.setId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDirector(dto.getDirector());
        movie.setCast(dto.getCast());
        movie.setDuration(dto.getDuration());
        movie.setGenre(dto.getGenre());
        movie.setLanguage(dto.getLanguage());
        movie.setRating(dto.getRating());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setStatus(dto.getStatus());
        return movie;
    }
}