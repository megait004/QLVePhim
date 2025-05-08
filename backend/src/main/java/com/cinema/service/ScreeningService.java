package com.cinema.service;

import com.cinema.dto.ScreeningDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.model.Movie;
import com.cinema.model.Screening;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ScreeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
public class ScreeningService {
    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private MovieRepository movieRepository;

    public ScreeningDTO createScreening(ScreeningDTO screeningDTO) {
        Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", screeningDTO.getMovieId()));

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setStartTime(screeningDTO.getStartTime());
        screening.setHallNumber(screeningDTO.getHallNumber());
        screening.setPrice(screeningDTO.getPrice());
        screening.setAvailableSeats(screeningDTO.getAvailableSeats());

        Screening savedScreening = screeningRepository.save(screening);
        return convertToDTO(savedScreening);
    }

    @Transactional
    public List<ScreeningDTO> createScreenings(List<ScreeningDTO> screeningDTOs) {
        return screeningDTOs.stream()
                .map(this::createScreening)
                .collect(Collectors.toList());
    }

    public List<ScreeningDTO> getScreeningsByMovie(Long movieId) {
        // Verify movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", "id", movieId);
        }

        LocalDateTime now = LocalDateTime.now();
        return screeningRepository.findByMovieIdAndStartTimeAfter(movieId, now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ScreeningDTO> getScreeningsByDateRange(LocalDateTime start, LocalDateTime end) {
        return screeningRepository.findByStartTimeBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ScreeningDTO getScreeningById(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
        return convertToDTO(screening);
    }

    public void deleteScreening(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screening", "id", id);
        }
        screeningRepository.deleteById(id);
    }

    private ScreeningDTO convertToDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setMovieId(screening.getMovie().getId());
        dto.setMovieTitle(screening.getMovie().getTitle());
        dto.setStartTime(screening.getStartTime());
        dto.setHallNumber(screening.getHallNumber());
        dto.setPrice(screening.getPrice());
        dto.setAvailableSeats(screening.getAvailableSeats());
        return dto;
    }
}