package com.kidfashion.ecommerce.kids_fashion_shop.controller.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kidfashion.ecommerce.kids_fashion_shop.model.PaymentTransaction;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.PaymentTransactionRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.service.SePayService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;

@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SePayWebhookController.class);

    private final SePayService sePayService;
    private final ShopOrderService shopOrderService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public SePayWebhookController(SePayService sePayService, ShopOrderService shopOrderService,
                                  PaymentTransactionRepository paymentTransactionRepository) {
        this.sePayService = sePayService;
        this.shopOrderService = shopOrderService;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    /**
     * Endpoint nhận Webhook từ SePay
     * URL cấu hình trên SePay: https://your-domain.render.com/api/sepay/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-SePay-Token", required = false) String sePayToken,
            @RequestBody Map<String, Object> payload) {
        
        log.info("Nhận Webhook từ SePay: {}", payload);

        // 1. Xác thực Token
        String tokenToVerify = (authHeader != null) ? authHeader : sePayToken;
        if (!this.sePayService.verifyWebhookToken(tokenToVerify)) {
            log.warn("Webhook SePay bị từ chối do sai Token! (Auth: {}, X-Token: {})", authHeader, sePayToken);
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            // 2. Trích xuất thông tin giao dịch
            String content = (String) payload.get("content"); 
            BigDecimal transferAmount = new BigDecimal(payload.get("transferAmount").toString());
            String transactionId = String.valueOf(payload.get("id"));
            String reference = payload.get("reference") != null ? String.valueOf(payload.get("reference")) : transactionId;

            // 3. Tìm mã đơn hàng từ nội dung chuyển khoản
            Long orderId = extractOrderId(content);
            if (orderId == null) {
                log.warn("[SePay] Không tìm thấy mã đơn hàng trong nội dung: {}", content);
                return ResponseEntity.ok("OK (Order ID not found)");
            }

            // 4. Lấy thông tin đơn hàng để so sánh tiền
            Optional<ShopOrder> orderOpt = this.shopOrderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                log.warn("[SePay] Không tìm thấy đơn hàng #{} trong hệ thống.", orderId);
                return ResponseEntity.ok("OK (Order not found)");
            }

            ShopOrder order = orderOpt.get();
            BigDecimal totalAmount = order.getTotalAmount();

            // KIỂM TRA SỐ TIỀN: Phải chuyển ĐỦ hoặc DƯ (làm tròn về số nguyên để tránh sai số thập phân)
            BigDecimal totalAmountRounded = totalAmount.setScale(0, java.math.RoundingMode.HALF_UP);
            BigDecimal transferAmountRounded = transferAmount.setScale(0, java.math.RoundingMode.HALF_UP);

            if (transferAmountRounded.compareTo(totalAmountRounded) < 0) {
                log.warn("[SePay] Đơn hàng #{}: Khách chuyển THIẾU tiền! (Cần: {}, Chuyển: {})", 
                         orderId, totalAmountRounded, transferAmountRounded);
                return ResponseEntity.ok("OK (Amount mismatch)");
            }

            // 5. Cập nhật đơn hàng thành công
            this.shopOrderService.completePayment(orderId, transactionId, content);
            log.info("[SePay] Xác nhận thành công đơn hàng #{}. (Giao dịch: {})", orderId, transactionId);

            // 6. Lưu Nhật ký giao dịch
            try {
                PaymentTransaction tx = new PaymentTransaction();
                tx.setShopOrder(order);
                tx.setAmount(transferAmount);
                tx.setTransactionRef(reference);
                tx.setPaymentMethod("SEPAY");
                tx.setStatus("SUCCESS");
                tx.setRawData(payload.toString());
                tx.setCreatedAt(LocalDateTime.now());
                this.paymentTransactionRepository.save(tx);
                log.info("[SePay] Đã lưu nhật ký giao dịch: {}", reference);
            } catch (Exception ex) {
                log.error("[SePay] Lỗi lưu nhật ký giao dịch: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("[SePay] Lỗi nghiêm trọng khi xử lý Webhook: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal Error");
        }

        return ResponseEntity.ok("OK");
    }

    private Long extractOrderId(String content) {
        if (content == null) return null;
        // 1. Tìm "DON HANG X" hoặc "THANH TOAN DON HANG X"
        Pattern p1 = Pattern.compile("(?:THANH TOAN )?DON HANG (\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(content);
        if (m1.find()) return Long.parseLong(m1.group(1));

        // 2. Tìm "DHX" (Ví dụ: DH123)
        Pattern p2 = Pattern.compile("DH(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(content);
        if (m2.find()) return Long.parseLong(m2.group(1));

        // 3. Tìm số có từ 1-6 chữ số xuất hiện độc lập
        Pattern p3 = Pattern.compile("\\b(\\d{1,6})\\b");
        Matcher m3 = p3.matcher(content);
        while (m3.find()) {
            try {
                return Long.parseLong(m3.group(1));
            } catch (Exception e) {}
        }
        return null;
    }
}
