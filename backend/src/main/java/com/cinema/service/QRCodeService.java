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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import jakarta.annotation.PostConstruct;

@Service
public class QRCodeService {
    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);

    @Value("${bank.account.number}")
    private String bankAccountNumber;

    @Value("${bank.account.name}")
    private String bankAccountName;

    @Value("${app.upload.dir:${user.home}/cinema/uploads/qrcodes}")
    private String uploadDir;

    @Value("${app.base.url:http://127.0.0.1:8080}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created QR code directory at: {}", uploadPath);
            }

            // Set quyền đọc/ghi cho thư mục
            uploadPath.toFile().setReadable(true, false);
            uploadPath.toFile().setWritable(true, false);
            uploadPath.toFile().setExecutable(true, false);

            logger.info("QR code directory permissions set successfully");
        } catch (Exception e) {
            logger.error("Error initializing QR code directory: {}", e.getMessage());
        }
    }

    public String generateQRCodeBase64(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] qrCodeBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public String generateAndSaveQRCode(String content) throws WriterException, IOException {
        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created QR code directory at: {}", uploadPath);
        }

        // Tạo tên file unique
        String fileName = UUID.randomUUID().toString() + ".png";
        Path filePath = uploadPath.resolve(fileName);

        // Tạo QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        // Lưu file
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

        // Tạo URL chuẩn
        String qrCodeUrl = baseUrl + "/api/qrcodes/" + fileName;
        logger.info("Generated QR code URL: {}", qrCodeUrl);

        return qrCodeUrl;
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