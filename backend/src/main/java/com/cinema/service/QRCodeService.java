package com.cinema.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@Service
public class QRCodeService {
    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);

    @Value("${bank.account.number}")
    private String bankAccountNumber;

    @Value("${bank.account.name}")
    private String bankAccountName;

    public String generateQRCodeBase64(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] qrCodeBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public String generatePaymentQRCode(double amount, String description) {
        try {
            // Tạo mã giao dịch unique
            String transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Tạo URL VietQR với template hTmycST
            String vietQrUrl = String.format(
                "https://api.vietqr.io/image/%s-%s-hTmycST.jpg?accountName=%s&amount=%d&addInfo=%s",
                "970448",  // Mã ngân hàng OCB
                bankAccountNumber,
                normalizeString(bankAccountName),
                (int)amount,
                description
            );

            logger.debug("Generated VietQR URL: {}", vietQrUrl);
            return vietQrUrl;
        } catch (Exception e) {
            logger.error("Error generating payment QR code", e);
            throw new RuntimeException("Could not generate payment QR code", e);
        }
    }

    private String normalizeString(String input) {
        // Chuẩn hóa chuỗi để phù hợp với URL
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("[^\\p{ASCII}]", "")
            .replaceAll(" ", "%20")
            .toUpperCase();
        return normalized;
    }
}