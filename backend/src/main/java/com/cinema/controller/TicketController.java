package com.cinema.controller;

import com.cinema.dto.TicketDTO;
import com.cinema.service.TicketService;
import com.cinema.exception.BusinessLogicException;
import com.cinema.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> bookTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        try {
            logger.info("Attempting to book ticket for user: {}, screening: {}, seat: {}",
                ticketDTO.getUserId(), ticketDTO.getScreeningId(), ticketDTO.getSeatNumber());
            TicketDTO ticket = ticketService.bookTicket(
                ticketDTO.getUserId(),
                ticketDTO.getScreeningId(),
                ticketDTO.getSeatNumber()
            );
            logger.info("Successfully booked ticket with id: {}", ticket.getId());
            return ResponseEntity.ok(ticket);
        } catch (ResourceNotFoundException | BusinessLogicException e) {
            logger.error("Failed to book ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while booking ticket", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @PostMapping("/book/batch")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> bookMultipleTickets(@Valid @RequestBody List<TicketDTO> ticketDTOs) {
        try {
            logger.info("Attempting to book {} tickets", ticketDTOs.size());
            List<TicketDTO> tickets = ticketService.bookMultipleTickets(ticketDTOs);
            logger.info("Successfully booked {} tickets", tickets.size());
            return ResponseEntity.ok(tickets);
        } catch (ResourceNotFoundException | BusinessLogicException e) {
            logger.error("Failed to book multiple tickets: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while booking multiple tickets", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserTickets(@PathVariable Long userId) {
        try {
            List<TicketDTO> tickets = ticketService.getUserTickets(userId);
            return ResponseEntity.ok(tickets);
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to get user tickets: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTicket(@PathVariable Long id) {
        try {
            TicketDTO ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to get ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusRequest statusRequest) {
        try {
            TicketDTO updatedTicket = ticketService.updateTicketStatus(id, statusRequest.getStatus());
            return ResponseEntity.ok(updatedTicket);
        } catch (ResourceNotFoundException | BusinessLogicException e) {
            logger.error("Failed to update ticket status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class TicketStatusRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}