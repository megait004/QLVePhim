package com.cinema.repository;

import com.cinema.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByScreeningId(Long screeningId);
    List<Ticket> findByUserIdAndStatus(Long userId, String status);
    boolean existsByScreeningIdAndSeatNumber(Long screeningId, String seatNumber);
    void deleteByScreeningId(Long screeningId);
    Optional<Ticket> findByQrCodeContaining(String transactionId);
}