package com.cinema.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Column(nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String status; // BOOKED, PAID, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String qrCode;
}