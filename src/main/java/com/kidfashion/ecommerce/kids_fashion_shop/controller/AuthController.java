package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.service.AppUserService;

@Controller
public class AuthController {

	private final AppUserService appUserService;

	public AuthController(AppUserService appUserService) {
		this.appUserService = appUserService;
	}

	@GetMapping("/login")
	public String loginPage() {
		return "auth/login";
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("pageTitle", "Đăng ký");
		return "auth/register";
	}

	@PostMapping("/register")
	public String registerSubmit(@RequestParam("email") String email, @RequestParam("password") String password,
			@RequestParam("fullName") String fullName, @RequestParam(name = "phone", required = false) String phone,
			@RequestParam(name = "address", required = false) String address, RedirectAttributes redirectAttributes) {
		Optional<com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser> existing = this.appUserService
				.findByEmail(email);
		if (existing.isPresent()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Email da duoc su dung.");
			return "redirect:/register";
		}
		if (password == null || password.length() < 6) {
			redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cần tối thiểu 6 ký tự.");
			return "redirect:/register";
		}
		this.appUserService.registerCustomer(email, password, fullName, phone, address);
		redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công. Vui lòng đăng nhập.");
		return "redirect:/login";
	}
}
