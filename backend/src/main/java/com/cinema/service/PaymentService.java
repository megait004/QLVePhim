package com.cinema.service;

import com.cinema.exception.ResourceNotFoundException;
import com.cinema.model.Ticket;
import com.cinema.repository.TicketRepository;
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URLEncoder;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final String PAYOS_API_URL = "https://api-merchant.payos.vn/v2/payment-requests";
    private final Gson gson = new Gson();

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${bank.account.number}")
    private String bankAccountNumber;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private TicketRepository ticketRepository;

    private String calculateHmac(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }

    public String createPaymentLink(Ticket ticket) {
        try {
            logger.info("Creating payment link for ticket: {}", ticket.getId());

            // Tạo orderCode là số nguyên duy nhất từ ticketId
            int orderCode = Math.abs(ticket.getId().intValue());
            int amount = ticket.getPrice().intValue();
            String description = String.format("Cinema-Ticket-%d-%s",
                ticket.getId(),
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            String returnUrl = "http://localhost:8080/api/payment/success?ticketId=" + ticket.getId();
            String cancelUrl = "http://localhost:8080/api/payment/cancel?ticketId=" + ticket.getId();
            long expiredAt = System.currentTimeMillis() / 1000 + 15 * 60;

            // Tạo QR code cho thanh toán
            String qrCode = qrCodeService.generatePaymentQRCode(amount, description);
            ticket.setQrCode(qrCode);

            // Tạo chuỗi để tính signature theo chuẩn PayOS
            String signData = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                amount,
                cancelUrl,
                description,
                orderCode,
                returnUrl
            );

            // Tính signature
            String signature = calculateHmac(signData, checksumKey);

            // Tạo request body theo chuẩn PayOS
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("orderCode", orderCode);
            requestBody.put("amount", amount);
            requestBody.put("description", description);
            requestBody.put("returnUrl", returnUrl);
            requestBody.put("cancelUrl", cancelUrl);
            requestBody.put("expiredAt", expiredAt);

            // Thêm thông tin người mua
            requestBody.put("buyerName", ticket.getUser().getFullName());
            requestBody.put("buyerEmail", ticket.getUser().getEmail());
            requestBody.put("buyerPhone", ticket.getUser().getPhoneNumber());
            requestBody.put("buyerAddress", "");

            // Thêm thông tin sản phẩm
            requestBody.put("items", List.of(
                Map.of(
                    "name", "Vé xem phim " + ticket.getScreening().getMovie().getTitle(),
                    "quantity", 1,
                    "price", amount
                )
            ));

            requestBody.put("signature", signature);

            String jsonBody = gson.toJson(requestBody);
            logger.debug("Request body: {}", jsonBody);
            logger.debug("Sign data: {}", signData);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(PAYOS_API_URL);
                request.setHeader("x-client-id", clientId);
                request.setHeader("x-api-key", apiKey);
                request.setHeader("Content-Type", "application/json");

                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                logger.info("Sending request to PayOS API");

                try (CloseableHttpResponse response = client.execute(request)) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    logger.debug("Response from PayOS: {}", responseBody);

                    Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);

                    // Kiểm tra response code từ PayOS
                    String code = (String) responseMap.get("code");
                    if ("00".equals(code)) {
                        // Thành công - lấy checkoutUrl từ data
                        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                        String checkoutUrl = (String) data.get("checkoutUrl");
                        logger.info("Successfully created payment link: {}", checkoutUrl);
                        return checkoutUrl;
                    } else {
                        // Có lỗi từ PayOS
                        String desc = (String) responseMap.get("desc");
                        logger.error("PayOS API error response: {}", responseBody);
                        throw new RuntimeException("PayOS API error: " + desc);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error creating payment link", e);
            throw new RuntimeException("Could not create payment link: " + e.getMessage());
        }
    }

    public String getPaymentQRCode(Ticket ticket) {
        return ticket.getQrCode();
    }

    public void handlePaymentCallback(String orderCode, String status) {
        logger.info("Payment callback received for order {} with status {}", orderCode, status);
        // Xử lý callback từ PayOS
        // Implement verification logic here
    }

    public boolean checkPaymentStatus(Long ticketId) {
        try {
            // Lấy thông tin vé
            Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

            // Kiểm tra thanh toán qua VietQR trước
            boolean isVietQRPaid = checkVietQRPayment(ticket);
            if (isVietQRPaid) {
                // Nếu thanh toán qua VietQR thành công, cập nhật trạng thái vé và PayOS
                ticket.setStatus("PAID");
                ticketRepository.save(ticket);
                updatePayOSStatus(ticket.getId());
                return true;
            }

            // Nếu VietQR chưa thanh toán, kiểm tra PayOS
            return checkPayOSPayment(ticket);
        } catch (Exception e) {
            logger.error("Error checking payment status for ticket {}: {}", ticketId, e.getMessage());
            return false;
        }
    }

    private boolean checkVietQRPayment(Ticket ticket) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Tạo URL kiểm tra giao dịch với VietQR
            String description = String.format("Cinema-Ticket-%d", ticket.getId());
            String checkUrl = String.format(
                "https://api.vietqr.io/v2/bank-trans/check-trans?bank_account_no=%s&bank_bin=%s&amount=%d&description=%s",
                bankAccountNumber,
                "970448", // Mã ngân hàng OCB
                ticket.getPrice().intValue(),
                URLEncoder.encode(description, "UTF-8")
            );

            HttpGet request = new HttpGet(checkUrl);
            request.setHeader("Accept", "application/json");
            request.setHeader("x-client-id", clientId);
            request.setHeader("x-api-key", apiKey);

            logger.info("Checking VietQR payment for ticket {}", ticket.getId());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                logger.debug("VietQR API response: {}", responseBody);
                Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);

                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                if (data != null && (boolean) data.getOrDefault("found", false)) {
                    // Nếu tìm thấy giao dịch VietQR, cập nhật PayOS
                    logger.info("VietQR payment found for ticket {}, updating PayOS status", ticket.getId());

                    // Tạo webhook callback để thông báo cho PayOS
                    notifyPayOSWebhook(ticket);

                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking VietQR payment: {}", e.getMessage());
            return false;
        }
    }

    private void notifyPayOSWebhook(Ticket ticket) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Tạo webhook URL từ PayOS
            String webhookUrl = "https://api-merchant.payos.vn/v2/payment-requests/" + ticket.getId() + "/hook";

            // Tạo dữ liệu webhook theo định dạng của PayOS
            Map<String, Object> webhookData = new LinkedHashMap<>();
            webhookData.put("orderCode", ticket.getId());
            webhookData.put("amount", ticket.getPrice().intValue());
            webhookData.put("description", String.format("Cinema-Ticket-%d", ticket.getId()));
            webhookData.put("status", "PAID");
            webhookData.put("paymentMethod", "BANK_TRANSFER");
            webhookData.put("bankAccount", bankAccountNumber);
            webhookData.put("bankName", "OCB");
            webhookData.put("transactionTime", System.currentTimeMillis());

            // Tạo chữ ký cho webhook
            String signData = String.format("amount=%d&description=%s&orderCode=%d&status=PAID",
                ticket.getPrice().intValue(),
                webhookData.get("description"),
                ticket.getId()
            );
            String signature = calculateHmac(signData, checksumKey);
            webhookData.put("signature", signature);

            String jsonBody = gson.toJson(webhookData);
            HttpPost request = new HttpPost(webhookUrl);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("x-client-id", clientId);
            request.setHeader("x-api-key", apiKey);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            logger.info("Sending webhook notification to PayOS for ticket {}", ticket.getId());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                logger.debug("PayOS webhook response: {}", responseBody);
            }
        } catch (Exception e) {
            logger.error("Error sending webhook to PayOS: {}", e.getMessage());
        }
    }

    private boolean checkPayOSPayment(Ticket ticket) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String checkUrl = PAYOS_API_URL + "/" + ticket.getId();

            HttpGet request = new HttpGet(checkUrl);
            request.setHeader("x-client-id", clientId);
            request.setHeader("x-api-key", apiKey);
            request.setHeader("Accept", "application/json");

            logger.info("Checking PayOS payment for ticket {}", ticket.getId());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                logger.debug("PayOS API response: {}", responseBody);
                Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);

                String code = (String) responseMap.get("code");
                if ("00".equals(code)) {
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                    String status = (String) data.get("status");

                    if ("PAID".equals(status)) {
                        logger.info("PayOS payment confirmed for ticket {}", ticket.getId());
                        ticket.setStatus("PAID");
                        ticketRepository.save(ticket);
                        return true;
                    }
                }

                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking PayOS payment: {}", e.getMessage());
            return false;
        }
    }

    private void updatePayOSStatus(Long ticketId) {
        try {
            // Tạo request body để cập nhật PayOS
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("orderCode", ticketId);
            requestBody.put("status", "PAID");

            String jsonBody = gson.toJson(requestBody);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(PAYOS_API_URL + "/" + ticketId + "/status");
                request.setHeader("x-client-id", clientId);
                request.setHeader("x-api-key", apiKey);
                request.setHeader("Content-Type", "application/json");

                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                logger.info("Updating PayOS status for ticket {}", ticketId);

                try (CloseableHttpResponse response = client.execute(request)) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    logger.debug("PayOS update response: {}", responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("Error updating PayOS status", e);
        }
    }
}