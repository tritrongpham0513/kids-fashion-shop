package com.kidfashion.ecommerce.kids_fashion_shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SePayService {

    @Value("${app.sepay.bank-id:MB}")
    private String bankId;

    @Value("${app.sepay.account-number:}")
    private String accountNumber;

    @Value("${app.sepay.account-name:}")
    private String accountName;

    @Value("${app.sepay.webhook-token:}")
    private String webhookToken;

    /**
     * Tạo URL mã QR thanh toán qua SePay (VietQR chuẩn)
     * Format: https://qr.sepay.vn/img?bank=<BANK_ID>&acc=<ACC_NUMBER>&template=compact&amount=<AMOUNT>&des=<DESCRIPTION>
     */
    public String generateQrUrl(ShopOrder order) {
        String amount = order.getTotalAmount().setScale(0, java.math.RoundingMode.HALF_UP).toString();
        String description = "THANH TOAN DON HANG " + order.getId();
        order.setSepayTransferContent(description); // Lưu lại nội dung để đối soát

        try {
            String encodedDes = URLEncoder.encode(description, StandardCharsets.UTF_8.toString());
            return String.format("https://qr.sepay.vn/img?bank=%s&acc=%s&template=compact&amount=%s&des=%s",
                    bankId, accountNumber, amount, encodedDes);
        } catch (Exception e) {
            return "";
        }
    }

    public boolean verifyWebhookToken(String tokenFromHeader) {
        if (webhookToken == null || webhookToken.isEmpty()) return true; // Nếu không cài token thì bỏ qua check
        if (tokenFromHeader == null) return false;
        
        // Loại bỏ tiền tố "Bearer " hoặc "Apikey " nếu có
        String finalToken = tokenFromHeader.trim();
        String lowerToken = finalToken.toLowerCase();
        
        if (lowerToken.startsWith("bearer ")) {
            finalToken = finalToken.substring(7).trim();
        } else if (lowerToken.startsWith("apikey ")) {
            finalToken = finalToken.substring(7).trim();
        }
        
        return webhookToken.trim().equals(finalToken);
    }
}
