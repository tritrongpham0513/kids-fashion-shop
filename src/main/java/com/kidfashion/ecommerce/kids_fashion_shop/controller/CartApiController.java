package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import com.kidfashion.ecommerce.kids_fashion_shop.config.ShopUserDetails;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartUpdateResponse;
import com.kidfashion.ecommerce.kids_fashion_shop.model.CartLineKey;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartPersistenceService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartSessionService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private static final Logger log = LoggerFactory.getLogger(CartApiController.class);

    private final CartSessionService cartSessionService;
    private final CartPersistenceService cartPersistenceService;

    public CartApiController(CartSessionService cartSessionService, CartPersistenceService cartPersistenceService) {
        this.cartSessionService = cartSessionService;
        this.cartPersistenceService = cartPersistenceService;
    }

    @PostMapping("/update")
    public ResponseEntity<CartUpdateResponse> updateQuantity(
            HttpSession session,
            @AuthenticationPrincipal ShopUserDetails principal,
            @RequestParam("lineKey") String lineKey,
            @RequestParam("quantity") int quantity) {

        try {
            this.cartSessionService.updateQuantity(session, lineKey, quantity);
            syncCartToDb(session, principal);

            CartUpdateResponse response = new CartUpdateResponse();
            response.setSuccess(true);
            
            Map<String, Integer> cartLineMap = this.cartSessionService.getCartLineMap(session);
            Integer updatedQty = cartLineMap.get(lineKey);
            response.setUpdatedQuantity(updatedQty != null ? updatedQty : 0);

            // Compute line subtotal
            CartLineKey.parse(lineKey);
            BigDecimal unitPrice = this.cartSessionService.buildLineViews(session).stream()
                    .filter(l -> l.getLineKey().equals(lineKey))
                    .findFirst()
                    .map(l -> l.getProduct().getPrice())
                    .orElse(BigDecimal.ZERO);
            
            response.setLineSubtotal(unitPrice.multiply(new BigDecimal(response.getUpdatedQuantity())));
            response.setCartSubtotal(this.cartSessionService.computeSubtotal(session));
            response.setCartItemsCount(cartLineMap.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating cart quantity", e);
            return ResponseEntity.badRequest().body(new CartUpdateResponse(false, "Có lỗi xảy ra khi cập nhật giỏ hàng."));
        }
    }

    private void syncCartToDb(HttpSession session, ShopUserDetails principal) {
        if (principal == null || principal.getAppUser() == null) {
            return;
        }
        try {
            this.cartPersistenceService.syncUserCartFromSession(session, principal.getAppUser().getId());
        } catch (Exception ex) {
            log.warn("Đồng bộ giỏ hàng xuống DB thất bại: {}", ex.toString());
        }
    }
}
