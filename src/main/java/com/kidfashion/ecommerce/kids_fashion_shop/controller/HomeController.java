package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.util.List;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ProductService;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	private final ProductService productService;

	public HomeController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/")
	public String home(Model model) {
		logger.debug("Truy cập trang chủ (/) - Bắt đầu chuẩn bị model");
		try {
			List<Product> newest = this.productService.findNewArrivals(10);
			List<Product> best = this.productService.findBestSellingForHome(10);

			model.addAttribute("newestProducts", newest != null ? newest : Collections.emptyList());
			model.addAttribute("bestSellingProducts", best != null ? best : Collections.emptyList());
			model.addAttribute("pageTitle", "Trang chủ");

			logger.debug("Trang chủ model chuẩn bị xong, trả về view: shop/home");
			return "shop/home";
		} catch (Exception e) {
			logger.error("Lỗi trong HomeController.home: ", e);
			// Đảm bảo các thuộc tính luôn tồn tại để Thymeleaf không bị lỗi khi render
			model.addAttribute("newestProducts", Collections.emptyList());
			model.addAttribute("bestSellingProducts", Collections.emptyList());
			model.addAttribute("pageTitle", "Trang chủ");
			model.addAttribute("error", "Lỗi dữ liệu: " + e.getMessage());
			return "shop/home";
		}
	}
}
