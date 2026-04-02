package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.config.ShopUserDetails;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CheckoutDiscountPreviewRequest;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CheckoutDiscountPreviewResponse;
import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartSessionService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.DiscountCodeService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

	private final CartSessionService cartSessionService;
	private final ShopOrderService shopOrderService;
	private final DiscountCodeService discountCodeService;

	public CheckoutController(CartSessionService cartSessionService, ShopOrderService shopOrderService,
			DiscountCodeService discountCodeService) {
		this.cartSessionService = cartSessionService;
		this.shopOrderService = shopOrderService;
		this.discountCodeService = discountCodeService;
	}

	@GetMapping("/checkout")
	@PreAuthorize("hasRole('CUSTOMER')")
	public String checkoutForm(HttpSession session, Model model) {
		List<CartLineDto> lines = this.cartSessionService.buildLineViews(session);
		if (lines.isEmpty()) {
			return "redirect:/cart";
		}
		BigDecimal subtotal = this.cartSessionService.computeSubtotal(session);
		model.addAttribute("cartLines", lines);
		model.addAttribute("cartSubtotal", subtotal);
		model.addAttribute("cartSubtotalText", formatMoneyVnd(subtotal));
		model.addAttribute("pageTitle", "Thanh toán");
		return "shop/checkout";
	}

	/**
	 * Xem trước mã giảm giá (gọi từ JS khi nhập mã) — không đặt hàng, không tăng usedCount.
	 */
	@PostMapping(value = "/checkout/preview-discount", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('CUSTOMER')")
	@ResponseBody
	public CheckoutDiscountPreviewResponse previewDiscount(HttpSession session,
			@RequestBody(required = false) CheckoutDiscountPreviewRequest body) {
		BigDecimal subtotal = this.cartSessionService.computeSubtotal(session);
		String subtotalText = formatMoneyVnd(subtotal);
		String code = "";
		if (body != null && body.getCode() != null) {
			code = body.getCode().trim();
		}
		if (code.isEmpty()) {
			return new CheckoutDiscountPreviewResponse(true, null, subtotalText, formatMoneyVnd(BigDecimal.ZERO),
					subtotalText);
		}
		Optional<DiscountCode> dcOpt = this.discountCodeService.findActiveByCodeText(code);
		if (dcOpt.isEmpty()) {
			return new CheckoutDiscountPreviewResponse(false, "Không tìm thấy mã giảm giá.", subtotalText,
					formatMoneyVnd(BigDecimal.ZERO), subtotalText);
		}
		DiscountCode dc = dcOpt.get();
		String err = this.discountCodeService.validateAndExplain(dc);
		if (err != null) {
			return new CheckoutDiscountPreviewResponse(false, err, subtotalText, formatMoneyVnd(BigDecimal.ZERO),
					subtotalText);
		}
		BigDecimal discount = this.discountCodeService.computeDiscountAmount(dc, subtotal);
		BigDecimal total = subtotal.subtract(discount);
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		}
		total = total.setScale(2, RoundingMode.HALF_UP);
		return new CheckoutDiscountPreviewResponse(true, "Đã áp dụng mã.", subtotalText, formatMoneyVnd(discount),
				formatMoneyVnd(total));
	}

	private static String formatMoneyVnd(BigDecimal amount) {
		BigDecimal v = amount == null ? BigDecimal.ZERO : amount.setScale(0, RoundingMode.HALF_UP);
		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat df = new DecimalFormat("#,##0", sym);
		return df.format(v) + " đ";
	}

	@PostMapping("/checkout/place")
	@PreAuthorize("hasRole('CUSTOMER')")
	public String placeOrder(HttpSession session, @AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam(name = "discountCode", required = false) String discountCode,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/login";
		}
		Long customerId = principal.getAppUser().getId();
		Map<Long, Integer> map = this.cartSessionService.getCartMap(session);
		try {
			ShopOrder order = this.shopOrderService.placeOrder(customerId, map, discountCode);
			this.cartSessionService.clear(session);
			redirectAttributes.addFlashAttribute("orderPlacedId", order.getId());
			return "redirect:/account/orders";
		} catch (IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("checkoutError", ex.getMessage());
			return "redirect:/checkout";
		}
	}
}
