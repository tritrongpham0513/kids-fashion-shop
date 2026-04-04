package com.kidfashion.ecommerce.kids_fashion_shop.controller.api;

import java.math.BigDecimal;
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

import com.kidfashion.ecommerce.kids_fashion_shop.model.OrderStatus;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.service.SePayService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;

@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SePayWebhookController.class);

    private final ShopOrderService shopOrderService;
    private final SePayService sePayService;

    public SePayWebhookController(ShopOrderService shopOrderService, SePayService sePayService) {
        this.shopOrderService = shopOrderService;
        this.sePayService = sePayService;
    }

    /**
     * Webhook Endpoint cho SePay
     * Tham khảo: https://docs.sepay.vn/tich-hop-webhook.html
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-SePay-Token", required = false) String sePayToken,
            @RequestBody Map<String, Object> payload) {
        
        log.info("Nhận Webhook từ SePay: {}", payload);

        // 1. Xác thực Token
        if (!this.sePayService.verifyWebhookToken(sePayToken)) {
            log.warn("Webhook SePay bị từ chối do sai Token!");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 2. Lấy nội dung chuyển khoản và số tiền
        String content = (String) payload.get("content"); // Ví dụ: "THANH TOAN DON HANG 123"
        if (content == null) return ResponseEntity.ok("OK (No content)");

        // 3. Tìm Mã đơn hàng trong nội dung (Regex)
        Long orderId = extractOrderId(content);
        if (orderId == null) {
            log.warn("[SePay] Không tìm thấy mã đơn hàng trong nội dung: {}", content);
            return ResponseEntity.ok("OK (No Order ID found)");
        }

        // 4. Cập nhật đơn hàng thông qua Service (Nguyên tử)
        try {
            // Lấy số tiền khách đã chuyển từ SePay (transferAmount)
            Object amountObj = payload.get("transferAmount");
            BigDecimal transferAmount = BigDecimal.ZERO;
            if (amountObj != null) {
                transferAmount = new BigDecimal(String.valueOf(amountObj));
            }

            // Lấy thông tin đơn hàng để so sánh tiền
            Optional<ShopOrder> orderOpt = this.shopOrderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                log.warn("[SePay] Không tìm thấy đơn hàng #{} trong hệ thống.", orderId);
                return ResponseEntity.ok("OK (Order not found)");
            }
            
            ShopOrder order = orderOpt.get();
            BigDecimal totalAmount = order.getTotalAmount();

            // KIỂM TRA SỐ TIỀN: Phải chuyển ĐỦ hoặc DƯ mới duyệt
            if (transferAmount.compareTo(totalAmount) < 0) {
                log.warn("[SePay] Đơn hàng #{}: Khách chuyển THIẾU tiền! (Cần: {}, Chuyển: {})", 
                         orderId, totalAmount, transferAmount);
                return ResponseEntity.ok("OK (Amount mismatch - too low)");
            }

            String transactionId = String.valueOf(payload.get("id"));
            this.shopOrderService.completePayment(orderId, transactionId, content);
            log.info("[SePay] Xác nhận thành công đơn hàng #{}. (Số tiền: {}, Giao dịch: {})", 
                     orderId, transferAmount, transactionId);

        } catch (Exception e) {
            log.error("[SePay] Lỗi nghiêm trọng khi đối soát đơn hàng #{}: {}", orderId, e.getMessage());
            return ResponseEntity.status(500).body("Internal Error");
        }

        return ResponseEntity.ok("OK");
    }

    private Long extractOrderId(String content) {
        if (content == null) return null;
        // 1. Tìm "DON HANG X" (Ưu tiên cao nhất)
        Pattern p1 = Pattern.compile("DON HANG (\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(content);
        if (m1.find()) return Long.parseLong(m1.group(1));

        // 2. Tìm "DHX" (Ví dụ: DH1)
        Pattern p2 = Pattern.compile("DH(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(content);
        if (m2.find()) return Long.parseLong(m2.group(1));

        // 3. Tìm số cuối cùng trong chuỗi (Dành cho nội dung chỉ có số)
        Pattern p3 = Pattern.compile("(\\d+)$");
        Matcher m3 = p3.matcher(content.trim());
        if (m3.find()) return Long.parseLong(m3.group(1));

        return null;
    }
}
