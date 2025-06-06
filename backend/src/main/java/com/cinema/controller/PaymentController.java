package com.cinema.controller;

import com.cinema.service.PaymentService;
import com.cinema.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/check-status/{ticketId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable Long ticketId) {
        try {
            // Kiểm tra trạng thái thanh toán của vé
            boolean isPaid = paymentService.checkPaymentStatus(ticketId);
            if (isPaid) {
                // Cập nhật trạng thái vé thành PAID
                ticketService.updateTicketStatus(ticketId, "PAID");
                return ResponseEntity.ok().body("Thanh toán thành công!");
            }
            return ResponseEntity.ok().body("Chưa nhận được thanh toán");
        } catch (Exception e) {
            logger.error("Error checking payment status", e);
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi kiểm tra trạng thái thanh toán");
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handlePaymentWebhook(@RequestBody PaymentWebhookRequest request) {
        logger.info("Received payment webhook: {}", request);

        try {
            paymentService.handlePaymentCallback(request.getOrderCode(), request.getStatus());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error processing payment webhook", e);
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }

    @GetMapping("/success")
    public String handlePaymentSuccess(@RequestParam String orderCode) {
        try {
            // Cập nhật trạng thái vé
            ticketService.updateTicketStatus(Long.parseLong(orderCode), "PAID");
            return "Thanh toán thành công! Vui lòng kiểm tra email để xem thông tin vé.";
        } catch (Exception e) {
            logger.error("Error handling payment success", e);
            return "Có lỗi xảy ra khi xử lý thanh toán. Vui lòng liên hệ hỗ trợ.";
        }
    }

    @GetMapping("/cancel")
    public String handlePaymentCancel(@RequestParam String orderCode) {
        try {
            // Cập nhật trạng thái vé
            ticketService.updateTicketStatus(Long.parseLong(orderCode), "CANCELLED");
            return "Bạn đã hủy thanh toán. Vé sẽ được hủy sau 15 phút nếu không thanh toán lại.";
        } catch (Exception e) {
            logger.error("Error handling payment cancel", e);
            return "Có lỗi xảy ra khi xử lý hủy thanh toán. Vui lòng liên hệ hỗ trợ.";
        }
    }
}

class PaymentWebhookRequest {
    private String orderCode;
    private String status;
    private Double amount;

    // Getters and Setters
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}