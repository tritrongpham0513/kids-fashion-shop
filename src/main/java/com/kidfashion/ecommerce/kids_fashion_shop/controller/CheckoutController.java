package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.config.ShopUserDetails;
import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CheckoutDiscountPreviewRequest;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CheckoutDiscountPreviewResponse;
import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartPersistenceService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CartSessionService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.DiscountCodeService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

	private final CartSessionService cartSessionService;
	private final CartPersistenceService cartPersistenceService;
	private final ShopOrderService shopOrderService;
	private final DiscountCodeService discountCodeService;

    @org.springframework.beans.factory.annotation.Value("${sepay.bank.acc}")
    private String sepayBankAcc;

    @org.springframework.beans.factory.annotation.Value("${sepay.bank.code}")
    private String sepayBankCode;

	public CheckoutController(CartSessionService cartSessionService, ShopOrderService shopOrderService,
			DiscountCodeService discountCodeService, CartPersistenceService cartPersistenceService) {
		this.cartSessionService = cartSessionService;
		this.shopOrderService = shopOrderService;
		this.discountCodeService = discountCodeService;
		this.cartPersistenceService = cartPersistenceService;
	}

	@GetMapping("/checkout")
	@PreAuthorize("hasRole('CUSTOMER')")
	public String checkoutForm(HttpSession session, @AuthenticationPrincipal ShopUserDetails principal, Model model,
			@RequestParam(name = "mode", required = false) String mode) {
		if ("all".equalsIgnoreCase(mode)) {
			this.cartSessionService.clearSelectedCheckoutKeys(session);
		}
		Optional<CartLineDto> buyNowLine = this.cartSessionService.buildBuyNowLineView(session);
		List<CartLineDto> lines;
		BigDecimal subtotal;
		if (buyNowLine.isPresent()) {
			// Nếu đang "Mua ngay" thì bỏ luôn trạng thái chọn nhiều dòng
			this.cartSessionService.clearSelectedCheckoutKeys(session);
			CartLineDto one = buyNowLine.get();
			lines = List.of(one);
			subtotal = one.getLineSubtotal();
			model.addAttribute("checkoutBuyNowOnly", Boolean.TRUE);
			model.addAttribute("checkoutSelectedOnly", Boolean.FALSE);
		} else {
			if (!this.cartSessionService.getSelectedCheckoutKeys(session).isEmpty()) {
				lines = this.cartSessionService.buildSelectedLineViews(session);
				if (lines.isEmpty()) {
					return "redirect:/cart";
				}
				subtotal = this.cartSessionService.computeSubtotalSelected(session);
				model.addAttribute("checkoutBuyNowOnly", Boolean.FALSE);
				model.addAttribute("checkoutSelectedOnly", Boolean.TRUE);
			} else {
				lines = this.cartSessionService.buildLineViews(session);
				if (lines.isEmpty()) {
					return "redirect:/cart";
				}
				subtotal = this.cartSessionService.computeSubtotal(session);
				model.addAttribute("checkoutBuyNowOnly", Boolean.FALSE);
				model.addAttribute("checkoutSelectedOnly", Boolean.FALSE);
			}
		}
		model.addAttribute("cartLines", lines);
		model.addAttribute("cartSubtotal", subtotal);
		model.addAttribute("cartSubtotalText", formatMoneyVnd(subtotal));
		model.addAttribute("pageTitle", "Thanh toán");
		String defaultAddress = "";
		if (principal != null && principal.getAppUser() != null && principal.getAppUser().getAddress() != null) {
			defaultAddress = principal.getAppUser().getAddress().trim();
		}
		model.addAttribute("defaultAddress", defaultAddress);
		model.addAttribute("hasDefaultAddress", !defaultAddress.isEmpty());
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
		BigDecimal subtotal;
		Optional<CartLineDto> buyNowLine = this.cartSessionService.buildBuyNowLineView(session);
		if (buyNowLine.isPresent()) {
			subtotal = buyNowLine.get().getLineSubtotal();
		} else if (!this.cartSessionService.getSelectedCheckoutKeys(session).isEmpty()) {
			subtotal = this.cartSessionService.computeSubtotalSelected(session);
		} else {
			subtotal = this.cartSessionService.computeSubtotal(session);
		}
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
			@RequestParam(name = "addressOption", required = false) String addressOption,
			@RequestParam(name = "shippingAddressInput", required = false) String shippingAddressInput,
			@RequestParam(name = "paymentMethod", required = false) String paymentMethod,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/login";
		}
		AppUser customer = principal.getAppUser();
		String shippingAddress = resolveShippingAddress(customer, addressOption, shippingAddressInput);
		Long customerId = principal.getAppUser().getId();
		Optional<CartLineDto> buyNowLine = this.cartSessionService.buildBuyNowLineView(session);
		final List<CartLineDto> lines;
		final boolean buyNowOnly;
		final boolean selectedOnly;
		if (buyNowLine.isPresent()) {
			// Nếu mua ngay thì bỏ trạng thái chọn nhiều dòng
			this.cartSessionService.clearSelectedCheckoutKeys(session);
			lines = List.of(buyNowLine.get());
			buyNowOnly = true;
			selectedOnly = false;
		} else {
			if (!this.cartSessionService.getSelectedCheckoutKeys(session).isEmpty()) {
				lines = this.cartSessionService.buildSelectedLineViews(session);
				selectedOnly = true;
			} else {
				lines = this.cartSessionService.buildLineViews(session);
				selectedOnly = false;
			}
			buyNowOnly = false;
			if (lines.isEmpty()) {
				redirectAttributes.addFlashAttribute("checkoutError", "Giỏ hàng trống.");
				return "redirect:/cart";
			}
		}
		try {
			ShopOrder order = this.shopOrderService.placeOrder(customerId, lines, discountCode, shippingAddress, paymentMethod);
			this.cartSessionService.clearBuyNowCheckout(session);
			if (selectedOnly) {
				// Giữ lại các món chưa chọn
				this.cartSessionService.removeSelectedLinesFromCart(session);
				this.cartPersistenceService.syncUserCartFromSession(session, customerId);
			} else if (!buyNowOnly) {
				// Thanh toán toàn bộ giỏ
				this.cartSessionService.clear(session);
				this.cartPersistenceService.clearUserCart(customerId);
			}
			
			redirectAttributes.addFlashAttribute("orderPlacedId", order.getId());
			
			if ("SEPAY".equalsIgnoreCase(order.getPaymentMethod())) {
				return "redirect:/checkout/payment/" + order.getId();
			}
			
			return "redirect:/account/orders";
		} catch (IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("checkoutError", ex.getMessage());
			return "redirect:/checkout";
		}
	}

	@GetMapping("/checkout/payment/{id}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public String paymentQr(@PathVariable("id") Long id, @AuthenticationPrincipal ShopUserDetails principal, Model model) {
		Optional<ShopOrder> orderOpt = this.shopOrderService.findById(id);
		if (orderOpt.isEmpty()) {
			return "redirect:/account/orders";
		}
		ShopOrder order = orderOpt.get();
		// Security check: only the owner can see the QR
		if (!order.getCustomer().getId().equals(principal.getAppUser().getId())) {
			return "redirect:/account/orders";
		}

		model.addAttribute("order", order);
		model.addAttribute("sepayBankAcc", sepayBankAcc);
		model.addAttribute("sepayBankCode", sepayBankCode);
		model.addAttribute("pageTitle", "Thanh toán đơn hàng #" + id);
		
		return "shop/payment-qr";
	}

	private String resolveShippingAddress(AppUser customer, String addressOption, String shippingAddressInput) {
		String option = addressOption == null ? "default" : addressOption.trim();
		if ("new".equalsIgnoreCase(option)) {
			return shippingAddressInput;
		}
		return customer == null ? null : customer.getAddress();
	}
}
