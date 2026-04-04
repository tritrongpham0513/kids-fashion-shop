package com.kidfashion.ecommerce.kids_fashion_shop.controller.api;

import com.kidfashion.ecommerce.kids_fashion_shop.dto.SePayWebhookPayload;
import com.kidfashion.ecommerce.kids_fashion_shop.model.OrderStatus;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {

    private final ShopOrderService shopOrderService;

    @Value("${sepay.api.key}")
    private String sepayApiKey;

    public SePayWebhookController(ShopOrderService shopOrderService) {
        this.shopOrderService = shopOrderService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestBody SePayWebhookPayload payload) {
        
        // 1. Verify API Key (Bearer Token)
        if (authHeader == null || !authHeader.equals("Bearer " + sepayApiKey)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 2. Parse Order ID from content (e.g. "KFS123" or similar)
        // We expect the description to contain a pattern like DH[orderId] or just the ID
        String content = payload.getContent();
        if (content == null || content.isEmpty()) {
            return ResponseEntity.ok("No content");
        }

        Long orderId = extractOrderId(content);
        if (orderId == null) {
            return ResponseEntity.ok("Order ID not found in content");
        }

        // 3. Find Order and update status if amount matches
        Optional<ShopOrder> orderOpt = shopOrderService.findById(orderId);
        if (orderOpt.isPresent()) {
            ShopOrder order = orderOpt.get();
            
            // Allow small difference if needed, but usually SePay content is exact
            if (payload.getTransferAmount().compareTo(order.getTotalAmount()) >= 0) {
                shopOrderService.updateStatus(orderId, OrderStatus.DA_THANH_TOAN);
                return ResponseEntity.ok("Order " + orderId + " marked as PAID");
            } else {
                return ResponseEntity.ok("Amount mismatch for order " + orderId);
            }
        }

        return ResponseEntity.ok("Order not found");
    }

    private Long extractOrderId(String content) {
        // Look for DH followed by numbers (e.g. DH123) or just numbers if they are clearly IDs
        // We will assume the prefix is 'DH' for 'Don Hang'
        Pattern pattern = Pattern.compile("DH(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        
        // Fallback: try to find any numbers if DH is not present
        Pattern numericPattern = Pattern.compile("(\\d+)");
        Matcher numMatcher = numericPattern.matcher(content);
        if (numMatcher.find()) {
            try {
                return Long.parseLong(numMatcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
}
