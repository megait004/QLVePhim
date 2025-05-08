package com.cinema.controller;

import com.cinema.dto.ScreeningDTO;
import com.cinema.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {
    @Autowired
    private ScreeningService screeningService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByMovie(@PathVariable Long movieId) {
        List<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(movieId);
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<ScreeningDTO> screenings = screeningService.getScreeningsByDateRange(start, end);
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreeningDTO> getScreening(@PathVariable Long id) {
        ScreeningDTO screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningDTO> createScreening(@Valid @RequestBody ScreeningDTO screeningDTO) {
        ScreeningDTO createdScreening = screeningService.createScreening(screeningDTO);
        return ResponseEntity.ok(createdScreening);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScreeningDTO>> createScreenings(@Valid @RequestBody List<ScreeningDTO> screeningDTOs) {
        List<ScreeningDTO> createdScreenings = screeningService.createScreenings(screeningDTOs);
        return ResponseEntity.ok(createdScreenings);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.ok().build();
    }
}