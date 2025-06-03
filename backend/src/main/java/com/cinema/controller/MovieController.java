package com.cinema.controller;

import com.cinema.dto.MovieDTO;
import com.cinema.dto.request.MovieSearchRequest;
import com.cinema.model.MovieStatus;
import com.cinema.service.MovieService;
import com.cinema.service.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/public/all")
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        List<MovieDTO> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/public/now-showing")
    public ResponseEntity<List<MovieDTO>> getNowShowing() {
        List<MovieDTO> movies = movieService.getNowShowingMovies();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/public/coming-soon")
    public ResponseEntity<List<MovieDTO>> getComingSoon() {
        List<MovieDTO> movies = movieService.getComingSoonMovies();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/public/hot")
    public ResponseEntity<List<MovieDTO>> getHotMovies() {
        List<MovieDTO> movies = movieService.getHotMovies();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/public/genre/{genre}")
    public ResponseEntity<List<MovieDTO>> getMoviesByGenre(@PathVariable String genre) {
        List<MovieDTO> movies = movieService.getMoviesByGenre(genre);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<MovieDTO> getMovie(@PathVariable Long id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> createMovie(
            @RequestParam("title") String title,
            @RequestParam("genre") String genre,
            @RequestParam("director") String director,
            @RequestParam("movieCast") String movieCast,
            @RequestParam("duration") Integer duration,
            @RequestParam("releaseDate") String releaseDate,
            @RequestParam("description") String description,
            @RequestParam(value = "status", defaultValue = "COMING_SOON") String status,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile) throws IOException {

        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle(title);
        movieDTO.setGenre(genre);
        movieDTO.setDirector(director);
        movieDTO.setCast(movieCast);
        movieDTO.setDuration(duration);
        movieDTO.setReleaseDate(LocalDate.parse(releaseDate).atStartOfDay());
        movieDTO.setDescription(description);
        movieDTO.setStatus(MovieStatus.valueOf(status));

        // Handle poster file if provided
        if (posterFile != null && !posterFile.isEmpty()) {
            String posterUrl = fileUploadService.uploadMoviePoster(posterFile);
            movieDTO.setPosterUrl(posterUrl);
        }

        MovieDTO createdMovie = movieService.createMovie(movieDTO);
        return ResponseEntity.ok(createdMovie);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDTO movieDTO) {
        MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/public/search")
    public ResponseEntity<List<MovieDTO>> searchMovies(@Valid @RequestBody MovieSearchRequest request) {
        List<MovieDTO> movies = movieService.searchMovies(
            request.getTitle(),
            request.getGenre(),
            request.getStatus(),
            request.getFromDate(),
            request.getToDate()
        );
        return ResponseEntity.ok(movies);
    }

    @PostMapping("/{id}/poster")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> updateMoviePoster(
            @PathVariable Long id,
            @RequestParam("posterFile") MultipartFile posterFile) {
        try {
            MovieDTO updatedMovie = movieService.updateMoviePoster(id, posterFile);
            return ResponseEntity.ok(updatedMovie);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, String>>> getAllStatuses() {
        List<Map<String, String>> statuses = Arrays.stream(MovieStatus.values())
            .map(status -> {
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put("value", status.name());
                statusMap.put("label", status.getDisplayName());
                return statusMap;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/public/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        List<String> genres = movieService.getAllUniqueGenres();
        return ResponseEntity.ok(genres);
    }
}