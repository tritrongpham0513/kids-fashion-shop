package com.kidfashion.ecommerce.kids_fashion_shop.controller.api;

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
            log.warn("Không tìm thấy mã đơn hàng trong nội dung: {}", content);
            return ResponseEntity.ok("OK (No Order ID found)");
        }

        // 4. Cập nhật đơn hàng
        Optional<ShopOrder> orderOpt = this.shopOrderService.findById(orderId);
        if (orderOpt.isPresent()) {
            ShopOrder order = orderOpt.get();
            if (order.getStatus() == OrderStatus.CHO_THANH_TOAN) {
                order.setStatus(OrderStatus.CHO_XAC_NHAN);
                order.setPaymentStatus("PAID");
                order.setSepayTransactionId(String.valueOf(payload.get("id")));
                this.shopOrderService.updateStatus(order.getId(), order.getStatus());
                log.info("Đơn hàng #{} đã được thanh toán tự động qua SePay.", orderId);
            }
        }

        return ResponseEntity.ok("OK");
    }

    private Long extractOrderId(String content) {
        if (content == null) return null;
        // Tìm số đứng sau chữ "DON HANG" hoặc số cuối cùng
        Pattern pattern = Pattern.compile("DON HANG (\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
