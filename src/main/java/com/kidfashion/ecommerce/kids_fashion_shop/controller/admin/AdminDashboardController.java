package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kidfashion.ecommerce.kids_fashion_shop.repository.AppUserRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ShopOrderRepository;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

	private final ProductRepository productRepository;
	private final ShopOrderRepository shopOrderRepository;
	private final AppUserRepository appUserRepository;

	public AdminDashboardController(ProductRepository productRepository, ShopOrderRepository shopOrderRepository,
			AppUserRepository appUserRepository) {
		this.productRepository = productRepository;
		this.shopOrderRepository = shopOrderRepository;
		this.appUserRepository = appUserRepository;
	}

	@GetMapping({"", "/"})
	public String dashboard(Model model) {
		long products = this.productRepository.count();
		long orders = this.shopOrderRepository.count();
		long customers = this.appUserRepository.findByRoleOrderByFullNameAsc(
				com.kidfashion.ecommerce.kids_fashion_shop.model.Role.CUSTOMER).size();
		model.addAttribute("countProducts", Long.valueOf(products));
		model.addAttribute("countOrders", Long.valueOf(orders));
		model.addAttribute("countCustomers", Long.valueOf(customers));
		model.addAttribute("pageTitle", "Bảng điều khiển");
		return "admin/dashboard";
	}
}
