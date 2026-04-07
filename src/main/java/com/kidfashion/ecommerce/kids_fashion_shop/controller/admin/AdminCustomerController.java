package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.service.AppUserService;

// Quản lý thông tin khách hàng và tài khoản người mua
@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

	private final AppUserService appUserService;

	public AdminCustomerController(AppUserService appUserService) {
		this.appUserService = appUserService;
	}

	@GetMapping("")
	public String list(Model model) {
		model.addAttribute("customers", this.appUserService.findCustomersOnly());
		model.addAttribute("pageTitle", "Khách hàng");
		return "admin/customers/list";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Long id, Model model) {
		Optional<AppUser> u = this.appUserService.findById(id);
		if (u.isEmpty() || u.get().getRole() != com.kidfashion.ecommerce.kids_fashion_shop.model.Role.CUSTOMER) {
			return "redirect:/admin/customers";
		}
		model.addAttribute("customer", u.get());
		model.addAttribute("pageTitle", "Cập nhật khách hàng");
		return "admin/customers/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("customer") AppUser formUser, RedirectAttributes redirectAttributes) {
		Optional<AppUser> existing = this.appUserService.findById(formUser.getId());
		if (existing.isEmpty()) {
			return "redirect:/admin/customers";
		}
		AppUser u = existing.get();
		if (u.getRole() != com.kidfashion.ecommerce.kids_fashion_shop.model.Role.CUSTOMER) {
			return "redirect:/admin/customers";
		}
		u.setFullName(formUser.getFullName());
		u.setPhone(formUser.getPhone());
		u.setAddress(formUser.getAddress());
		this.appUserService.save(u);
		
		redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin khách hàng thành công!");
		
		return "redirect:/admin/customers";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		this.appUserService.deleteCustomerById(id);
		redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản khách hàng khỏi hệ thống.");
		return "redirect:/admin/customers";
	}
}
