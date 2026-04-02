package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import com.kidfashion.ecommerce.kids_fashion_shop.service.DiscountCodeService;

@Controller
@RequestMapping("/admin/discounts")
public class AdminDiscountController {

	private final DiscountCodeService discountCodeService;

	public AdminDiscountController(DiscountCodeService discountCodeService) {
		this.discountCodeService = discountCodeService;
	}

	@GetMapping("")
	public String list(Model model) {
		model.addAttribute("codes", this.discountCodeService.findAll());
		model.addAttribute("pageTitle", "Mã giảm giá");
		return "admin/discounts/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		DiscountCode d = new DiscountCode();
		model.addAttribute("discount", d);
		model.addAttribute("pageTitle", "Thêm mã giảm giá");
		return "admin/discounts/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("discount") DiscountCode discount) {
		this.discountCodeService.save(discount);
		return "redirect:/admin/discounts";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Long id, Model model) {
		Optional<DiscountCode> d = this.discountCodeService.findById(id);
		if (d.isEmpty()) {
			return "redirect:/admin/discounts";
		}
		model.addAttribute("discount", d.get());
		model.addAttribute("pageTitle", "Sửa mã giảm giá");
		return "admin/discounts/form";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Long id) {
		this.discountCodeService.deleteById(id);
		return "redirect:/admin/discounts";
	}
}
