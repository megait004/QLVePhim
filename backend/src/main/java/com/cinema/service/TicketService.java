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
import com.google.zxing.WriterException;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    @Value("${bank.account.number}")
    private String bankAccountNumber;

    @Value("${bank.account.name}")
    private String bankAccountName;

    @Transactional
    public TicketDTO bookTicket(Long userId, Long screeningId, String seatNumber) {
        logger.debug("Bắt đầu đặt vé cho user {} suất chiếu {} ghế {}", userId, screeningId, seatNumber);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        logger.debug("Tìm thấy user: {}", user.getUsername());

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));
        logger.debug("Tìm thấy suất chiếu: {} - Phim: {}", screening.getId(),
            screening.getMovie() != null ? screening.getMovie().getTitle() : "null");

        if (screening.getMovie() == null) {
            throw new BusinessLogicException("Không tìm thấy thông tin phim cho suất chiếu này");
        }

        if (ticketRepository.existsByScreeningIdAndSeatNumber(screeningId, seatNumber)) {
            throw new BusinessLogicException("Ghế " + seatNumber + " đã được đặt");
        }

        if (screening.getAvailableSeats() <= 0) {
            throw new BusinessLogicException("Đã hết ghế cho suất chiếu này");
        }

        try {
            // Tạo vé với trạng thái chờ thanh toán
            Ticket ticket = new Ticket();
            ticket.setUser(user);
            ticket.setScreening(screening);
            ticket.setSeatNumber(seatNumber);
            ticket.setBookingTime(LocalDateTime.now());
            ticket.setPrice(screening.getPrice());
            ticket.setStatus("PENDING_PAYMENT");

            // Lưu vé vào database
            Ticket savedTicket = ticketRepository.save(ticket);
            logger.debug("Đã lưu vé với ID: {}", savedTicket.getId());

            // Cập nhật số ghế còn trống
            screening.setAvailableSeats(screening.getAvailableSeats() - 1);
            screeningRepository.save(screening);

            // Tạo link thanh toán PayOS
            String paymentLink = paymentService.createPaymentLink(savedTicket);

            // Lấy QR code từ response của PayOS
            String qrCode = paymentService.getPaymentQRCode(savedTicket);
            savedTicket.setQrCode(qrCode);
            ticketRepository.save(savedTicket);

            // Gửi email xác nhận với QR code
            emailService.sendTicketConfirmation(savedTicket, qrCode);
            logger.debug("Đã gửi email xác nhận");

            return convertToDTO(savedTicket);

        } catch (Exception e) {
            logger.error("Lỗi khi đặt vé: ", e);
            throw new BusinessLogicException("Có lỗi xảy ra khi xử lý đặt vé: " + e.getMessage());
        }
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

    public List<TicketDTO> getTicketsByScreeningId(Long screeningId) {
        if (!screeningRepository.existsById(screeningId)) {
            throw new ResourceNotFoundException("Screening", "id", screeningId);
        }
        return ticketRepository.findByScreeningId(screeningId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    @Transactional
    public TicketDTO updateTicketStatusByTransaction(String transactionId, String status) {
        // Tìm vé dựa vào mã giao dịch trong description của QR code
        Ticket ticket = ticketRepository.findByQrCodeContaining(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "transactionId", transactionId));

        ticket.setStatus(status);
        Ticket updatedTicket = ticketRepository.save(ticket);

        logger.info("Updated ticket {} status to {}", ticket.getId(), status);
        return convertToDTO(updatedTicket);
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