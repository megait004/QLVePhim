package com.cinema.service;

import com.cinema.dto.TicketDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.exception.BusinessLogicException;
import com.cinema.model.Screening;
import com.cinema.model.Ticket;
import com.cinema.model.User;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.TicketRepository;
import com.cinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Transactional
    public TicketDTO bookTicket(Long userId, Long screeningId, String seatNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));

        if (ticketRepository.existsByScreeningIdAndSeatNumber(screeningId, seatNumber)) {
            throw new BusinessLogicException("Seat " + seatNumber + " is already booked");
        }

        if (screening.getAvailableSeats() <= 0) {
            throw new BusinessLogicException("No seats available for this screening");
        }

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setScreening(screening);
        ticket.setSeatNumber(seatNumber);
        ticket.setBookingTime(LocalDateTime.now());
        ticket.setPrice(screening.getPrice());
        ticket.setStatus("BOOKED");

        ticket.setQrCode(generateQRCode(ticket));

        screening.setAvailableSeats(screening.getAvailableSeats() - 1);
        screeningRepository.save(screening);

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
    }

    @Transactional
    public List<TicketDTO> bookMultipleTickets(List<TicketDTO> ticketDTOs) {
        return ticketDTOs.stream()
                .map(dto -> bookTicket(dto.getUserId(), dto.getScreeningId(), dto.getSeatNumber()))
                .collect(Collectors.toList());
    }

    public List<TicketDTO> getUserTickets(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return ticketRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO updateTicketStatus(Long id, String status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        if (status.equals("CANCELLED")) {
            Screening screening = ticket.getScreening();
            screening.setAvailableSeats(screening.getAvailableSeats() + 1);
            screeningRepository.save(screening);
        }

        ticket.setStatus(status);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return convertToDTO(updatedTicket);
    }

    private String generateQRCode(Ticket ticket) {
        return "QR_" + ticket.getScreening().getId() + "_" + ticket.getSeatNumber();
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setUserId(ticket.getUser().getId());
        dto.setUsername(ticket.getUser().getUsername());
        dto.setScreeningId(ticket.getScreening().getId());
        dto.setMovieTitle(ticket.getScreening().getMovie().getTitle());
        dto.setScreeningTime(ticket.getScreening().getStartTime());
        dto.setHallNumber(ticket.getScreening().getHallNumber());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setBookingTime(ticket.getBookingTime());
        dto.setPrice(ticket.getPrice());
        dto.setStatus(ticket.getStatus());
        dto.setQrCode(ticket.getQrCode());
        return dto;
    }
}