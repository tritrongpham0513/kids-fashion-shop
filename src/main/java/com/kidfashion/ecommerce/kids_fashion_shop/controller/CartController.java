package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.kidfashion.ecommerce.kids_fashion_shop.config.ShopUserDetails;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.model.CartLineKey;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartPersistenceService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartSessionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

	private static final Logger log = LoggerFactory.getLogger(CartController.class);

	private final CartSessionService cartSessionService;
	private final CartPersistenceService cartPersistenceService;

	public CartController(CartSessionService cartSessionService, CartPersistenceService cartPersistenceService) {
		this.cartSessionService = cartSessionService;
		this.cartPersistenceService = cartPersistenceService;
	}

	private void syncCartToDb(HttpSession session, ShopUserDetails principal) {
		if (principal == null || principal.getAppUser() == null) {
			return;
		}
		try {
			this.cartPersistenceService.syncUserCartFromSession(session, principal.getAppUser().getId());
		} catch (Exception ex) {
			log.warn("Đồng bộ giỏ hàng xuống DB thất bại (giỏ phiên vẫn dùng được): {}", ex.toString());
		}
	}

	@GetMapping("/cart")
	public String viewCart(HttpSession session, Model model) {
		this.cartSessionService.clearBuyNowCheckout(session);
		this.cartSessionService.clearSelectedCheckoutKeys(session);
		List<CartLineDto> lines = this.cartSessionService.buildLineViews(session);
		BigDecimal subtotal = this.cartSessionService.computeSubtotal(session);
		model.addAttribute("cartLines", lines);
		model.addAttribute("cartSubtotal", subtotal);
		model.addAttribute("pageTitle", "Giỏ hàng");
		return "shop/cart";
	}

	@PostMapping("/cart/checkout-selected")
	public String checkoutSelected(HttpSession session,
			@RequestParam(name = "selectedLineKeys", required = false) List<String> selectedLineKeys) {
		// Mua ngay và chọn giỏ là 2 chế độ khác nhau; vào trang checkout thì ưu tiên chọn giỏ nếu có.
		this.cartSessionService.clearBuyNowCheckout(session);
		if (selectedLineKeys == null || selectedLineKeys.isEmpty()) {
			return "redirect:/cart";
		}
		this.cartSessionService.setSelectedCheckoutKeys(session, selectedLineKeys);
		return "redirect:/checkout";
	}

	@PostMapping("/cart/add")
	public String addToCart(HttpSession session, @AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam("productId") Long productId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity,
			@RequestParam(name = "colorLabel", required = false) String colorLabel,
			@RequestParam(name = "sizeLabel", required = false) String sizeLabel) {
		this.cartSessionService.addProduct(session, productId, quantity, colorLabel, sizeLabel);
		syncCartToDb(session, principal);
		return "redirect:/cart";
	}

	@PostMapping("/cart/buy-now")
	public String buyNow(HttpSession session, @AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam("productId") Long productId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity,
			@RequestParam(name = "colorLabel", required = false) String colorLabel,
			@RequestParam(name = "sizeLabel", required = false) String sizeLabel) {
		this.cartSessionService.setBuyNowCheckout(session, productId, quantity, colorLabel, sizeLabel);
		return "redirect:/checkout";
	}

	@PostMapping("/cart/update")
	public String updateLine(HttpSession session, @AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam("productId") Long productId,
			@RequestParam(name = "colorLabel", required = false) String colorLabel,
			@RequestParam(name = "sizeLabel", required = false) String sizeLabel,
			@RequestParam("quantity") int quantity) {
		String lineKey = CartLineKey.encode(productId, colorLabel, sizeLabel);
		this.cartSessionService.updateQuantity(session, lineKey, quantity);
		syncCartToDb(session, principal);
		return "redirect:/cart";
	}

	@PostMapping("/cart/remove")
	public String removeLine(HttpSession session, @AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam("productId") Long productId,
			@RequestParam(name = "colorLabel", required = false) String colorLabel,
			@RequestParam(name = "sizeLabel", required = false) String sizeLabel) {
		String lineKey = CartLineKey.encode(productId, colorLabel, sizeLabel);
		this.cartSessionService.removeLine(session, lineKey);
		syncCartToDb(session, principal);
		return "redirect:/cart";
	}
}
