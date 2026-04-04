package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import java.util.List;
import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kidfashion.ecommerce.kids_fashion_shop.model.OrderStatus;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ShopOrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

	private final ShopOrderService shopOrderService;

	public AdminOrderController(ShopOrderService shopOrderService) {
		this.shopOrderService = shopOrderService;
	}

	@GetMapping("")
	public String list(Model model) {
		List<ShopOrder> orders = this.shopOrderService.findAllNewsestFirst();
		model.addAttribute("orders", orders);
		model.addAttribute("pageTitle", "Đơn hàng");
		return "admin/orders/list";
	}

	@GetMapping("/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		Optional<ShopOrder> o = this.shopOrderService.findById(id);
		if (o.isEmpty()) {
			return "redirect:/admin/orders";
		}
		model.addAttribute("order", o.get());
		model.addAttribute("statuses", OrderStatus.values());
		model.addAttribute("pageTitle", "Chi tiết đơn");
		return "admin/orders/detail";
	}

	@PostMapping("/{id}/status")
	public String updateStatus(@PathVariable("id") Long id, @RequestParam("status") OrderStatus status, RedirectAttributes redirectAttributes) {
		this.shopOrderService.updateStatus(id, status);
		redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng thành công!");
		return "redirect:/admin/orders/" + id;
	}
}
