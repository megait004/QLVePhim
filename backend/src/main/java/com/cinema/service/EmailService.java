package com.cinema.service;

import com.cinema.model.Ticket;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicketConfirmation(Ticket ticket, String qrCodeUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(ticket.getUser().getEmail());
        helper.setSubject("Xác nhận đặt vé xem phim - Cinema");

        String content = buildEmailContent(ticket, qrCodeUrl);
        helper.setText(content, true);

        logger.debug("Sending email with QR code URL: {}", qrCodeUrl);
        mailSender.send(message);
    }

    private String buildEmailContent(Ticket ticket, String qrCodeUrl) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html><head><meta charset='UTF-8'></head>");
        content.append("<body style='font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");

        // Header
        content.append("<h1 style='color: #6d28d9; margin-top: 0; text-align: center;'>Xác nhận đặt vé</h1>");
        content.append("<p style='color: #4b5563;'>Xin chào ").append(ticket.getUser().getUsername()).append(",</p>");
        content.append("<p style='color: #4b5563;'>Cảm ơn bạn đã đặt vé xem phim tại Cinema. Dưới đây là thông tin chi tiết vé của bạn:</p>");

        // Ticket Info
        content.append("<div style='background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        content.append("<h2 style='color: #374151; margin-top: 0;'>").append(ticket.getScreening().getMovie().getTitle()).append("</h2>");
        content.append("<p><strong>Thời gian:</strong> ").append(ticket.getScreening().getStartTime().format(DATE_FORMATTER)).append("</p>");
        content.append("<p><strong>Phòng chiếu:</strong> ").append(ticket.getScreening().getHallNumber()).append("</p>");
        content.append("<p><strong>Ghế:</strong> ").append(ticket.getSeatNumber()).append("</p>");
        content.append("<p><strong>Giá vé:</strong> ").append(String.format("%,d", ticket.getPrice().intValue())).append(" VNĐ</p>");
        content.append("</div>");

        // Payment QR Code
        content.append("<div style='text-align: center; margin: 30px 0;'>");
        content.append("<p style='color: #4b5563; margin-bottom: 15px;'><strong>Vui lòng quét mã QR bên dưới để thanh toán:</strong></p>");
        content.append("<img src='").append(qrCodeUrl).append("' ");
        content.append("alt='QR Code Thanh toán' style='max-width: 300px; width: 100%; height: auto; border: 1px solid #e5e7eb; border-radius: 8px;'/>");

        // Thêm nút kiểm tra thanh toán
        content.append("<div style='margin-top: 20px;'>");
        content.append("<p style='color: #4b5563; margin-bottom: 10px;'>Sau khi chuyển khoản, vui lòng nhấn nút bên dưới để kiểm tra trạng thái thanh toán:</p>");
        content.append("<a href='http://localhost:8080/payment-status.html?ticketId=").append(ticket.getId()).append("' ");
        content.append("style='display: inline-block; background-color: #6d28d9; color: white; padding: 12px 24px; ");
        content.append("text-decoration: none; border-radius: 6px; font-weight: bold;'>Kiểm tra thanh toán</a>");
        content.append("</div>");
        content.append("</div>");

        // Footer
        content.append("<p style='color: #ef4444; margin: 20px 0;'><em>Lưu ý: Vé chỉ có hiệu lực sau khi thanh toán thành công.</em></p>");
        content.append("<p style='color: #4b5563; text-align: center;'>Chúc bạn có buổi xem phim vui vẻ!</p>");

        content.append("</div></body></html>");

        return content.toString();
    }
}