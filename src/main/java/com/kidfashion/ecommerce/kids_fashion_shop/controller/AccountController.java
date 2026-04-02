package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.config.ShopUserDetails;
import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.service.AppUserService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;

@Controller
@RequestMapping("/account")
public class AccountController {

	private final ShopOrderService shopOrderService;
	private final AppUserService appUserService;

	public AccountController(ShopOrderService shopOrderService, AppUserService appUserService) {
		this.shopOrderService = shopOrderService;
		this.appUserService = appUserService;
	}

	@GetMapping("/profile")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	public String profileForm(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
		if (principal == null) {
			return "redirect:/login";
		}
		AppUser u = this.appUserService.findById(principal.getAppUser().getId()).orElseThrow();
		model.addAttribute("user", u);
		model.addAttribute("pageTitle", "Thông tin tài khoản");
		return "shop/account-profile";
	}

	@PostMapping("/profile")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	public String profileSave(@AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam("fullName") String fullName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "currentPassword", required = false) String currentPassword,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "confirmPassword", required = false) String confirmPassword,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/login";
		}
		Long id = principal.getAppUser().getId();
		try {
			AppUser updated = this.appUserService.updateProfile(id, fullName, phone, address);
			this.refreshAuthentication(updated);
			boolean wantChange = newPassword != null && !newPassword.isBlank();
			if (wantChange) {
				if (confirmPassword == null) {
					throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
				}
				if (newPassword == null) {
					throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
				}
				if (!newPassword.equals(confirmPassword)) {
					throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
				}
				updated = this.appUserService.changePassword(id, currentPassword, newPassword);
				this.refreshAuthentication(updated);
			}
			redirectAttributes.addFlashAttribute("profileSuccess", "Đã lưu thông tin tài khoản.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("profileError", ex.getMessage());
		}
		return "redirect:/account/profile";
	}

	@GetMapping("/change-password")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	public String changePasswordForm(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
		if (principal == null) {
			return "redirect:/login";
		}
		AppUser u = this.appUserService.findById(principal.getAppUser().getId()).orElseThrow();
		model.addAttribute("user", u);
		model.addAttribute("pageTitle", "Đổi mật khẩu");
		return "shop/change-password";
	}

	@PostMapping("/change-password")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	public String changePasswordSubmit(@AuthenticationPrincipal ShopUserDetails principal,
			@RequestParam(value = "currentPassword", required = false) String currentPassword,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "confirmPassword", required = false) String confirmPassword,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/login";
		}
		Long id = principal.getAppUser().getId();
		try {
			if (currentPassword == null || currentPassword.isBlank()) {
				throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại.");
			}
			if (newPassword == null || newPassword.isBlank()) {
				throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới.");
			}
			if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
				throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
			}
			AppUser updated = this.appUserService.changePassword(id, currentPassword, newPassword);
			this.refreshAuthentication(updated);
			redirectAttributes.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
		}
		return "redirect:/account/change-password";
	}

	private void refreshAuthentication(AppUser fresh) {
		ShopUserDetails details = new ShopUserDetails(fresh);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(details,
				details.getPassword(), details.getAuthorities());
		if (auth != null) {
			newAuth.setDetails(auth.getDetails());
		}
		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}

	@GetMapping("/orders")
	public String myOrders(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
		if (principal == null) {
			return "redirect:/login";
		}
		List<ShopOrder> orders = this.shopOrderService.findForCustomer(principal.getAppUser().getId());
		model.addAttribute("orders", orders);
		model.addAttribute("pageTitle", "Đơn hàng của tôi");
		return "shop/my-orders";
	}

	@GetMapping("/orders/{id}")
	public String orderDetail(@AuthenticationPrincipal ShopUserDetails principal, @PathVariable("id") Long id,
			Model model) {
		if (principal == null) {
			return "redirect:/login";
		}
		Optional<ShopOrder> o = this.shopOrderService.findById(id);
		if (o.isEmpty()) {
			return "redirect:/account/orders";
		}
		if (!o.get().getCustomer().getId().equals(principal.getAppUser().getId())) {
			return "redirect:/account/orders";
		}
		model.addAttribute("order", o.get());
		model.addAttribute("pageTitle", "Chi tiết đơn hàng");
		return "shop/order-detail";
	}
}
