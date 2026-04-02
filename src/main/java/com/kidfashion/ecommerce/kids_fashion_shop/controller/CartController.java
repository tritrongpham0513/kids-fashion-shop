package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartSessionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

	private final CartSessionService cartSessionService;

	public CartController(CartSessionService cartSessionService) {
		this.cartSessionService = cartSessionService;
	}

	@GetMapping("/cart")
	public String viewCart(HttpSession session, Model model) {
		List<CartLineDto> lines = this.cartSessionService.buildLineViews(session);
		BigDecimal subtotal = this.cartSessionService.computeSubtotal(session);
		model.addAttribute("cartLines", lines);
		model.addAttribute("cartSubtotal", subtotal);
		model.addAttribute("pageTitle", "Giỏ hàng");
		return "shop/cart";
	}

	@PostMapping("/cart/add")
	public String addToCart(HttpSession session, @RequestParam("productId") Long productId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity) {
		this.cartSessionService.addProduct(session, productId, quantity);
		return "redirect:/cart";
	}

	@PostMapping("/cart/buy-now")
	public String buyNow(HttpSession session, @RequestParam("productId") Long productId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity) {
		this.cartSessionService.addProduct(session, productId, quantity);
		return "redirect:/checkout";
	}

	@PostMapping("/cart/update/{productId}")
	public String updateLine(HttpSession session, @PathVariable("productId") Long productId,
			@RequestParam("quantity") int quantity) {
		this.cartSessionService.updateQuantity(session, productId, quantity);
		return "redirect:/cart";
	}

	@PostMapping("/cart/remove/{productId}")
	public String removeLine(HttpSession session, @PathVariable("productId") Long productId) {
		this.cartSessionService.removeLine(session, productId);
		return "redirect:/cart";
	}
}
