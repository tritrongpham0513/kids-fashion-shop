package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CategoryService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

	private final ProductService productService;
	private final CategoryService categoryService;

	public AdminProductController(ProductService productService, CategoryService categoryService) {
		this.productService = productService;
		this.categoryService = categoryService;
	}

	@GetMapping("")
	public String list(Model model) {
		model.addAttribute("products", this.productService.findAll());
		model.addAttribute("pageTitle", "Quản lý sản phẩm");
		return "admin/products/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		Product p = new Product();
		p.setNewArrival(Boolean.FALSE);
		p.setBestSeller(Boolean.FALSE);
		p.setAdminHot(Boolean.FALSE);
		p.setStockQuantity(Integer.valueOf(0));
		p.setCreatedAt(LocalDateTime.now());
		model.addAttribute("product", p);
		model.addAttribute("categories", this.categoryService.findAllSortedByName());
		model.addAttribute("pageTitle", "Thêm sản phẩm");
		return "admin/products/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("product") Product product, @RequestParam("categoryId") Long categoryId) {
		Optional<Category> cat = this.categoryService.findById(categoryId);
		if (cat.isPresent()) {
			product.setCategory(cat.get());
		}
		if (product.getId() != null) {
			Optional<Product> existing = this.productService.findById(product.getId());
			if (existing.isPresent()) {
				Product e = existing.get();
				product.setCreatedAt(e.getCreatedAt());
				product.setViewCount(e.getViewCount());
				product.setSearchImpressionCount(e.getSearchImpressionCount());
				product.setSearchClickCount(e.getSearchClickCount());
				product.setHotScore(e.getHotScore());
			} else {
				product.setCreatedAt(LocalDateTime.now());
			}
		} else {
			product.setCreatedAt(LocalDateTime.now());
		}
		if (product.getAdminHot() == null) {
			product.setAdminHot(Boolean.FALSE);
		}
		this.productService.save(product);
		return "redirect:/admin/products";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Long id, Model model) {
		Optional<Product> p = this.productService.findById(id);
		if (p.isEmpty()) {
			return "redirect:/admin/products";
		}
		model.addAttribute("product", p.get());
		model.addAttribute("categories", this.categoryService.findAllSortedByName());
		model.addAttribute("pageTitle", "Sửa sản phẩm");
		return "admin/products/form";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			this.productService.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm.");
		} catch (IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		} catch (DataIntegrityViolationException ex) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Không thể xóa sản phẩm vì còn dữ liệu liên quan. Vui lòng thử lại sau.");
		}
		return "redirect:/admin/products";
	}
}
