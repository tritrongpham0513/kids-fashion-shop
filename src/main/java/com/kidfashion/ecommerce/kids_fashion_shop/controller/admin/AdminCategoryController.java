package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

	private final CategoryService categoryService;

	public AdminCategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping("")
	public String list(Model model) {
		List<Category> categories = this.categoryService.findAllSortedByName();
		Map<Long, Long> productCountByCategory = new HashMap<Long, Long>();
		for (int i = 0; i < categories.size(); i++) {
			Category c = categories.get(i);
			productCountByCategory.put(c.getId(), this.categoryService.countProductsByCategoryId(c.getId()));
		}
		model.addAttribute("categories", categories);
		model.addAttribute("productCountByCategory", productCountByCategory);
		model.addAttribute("pageTitle", "Quản lý danh mục");
		return "admin/categories/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		Category c = new Category();
		model.addAttribute("category", c);
		model.addAttribute("pageTitle", "Thêm danh mục");
		return "admin/categories/form";
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("category") Category category, BindingResult bindingResult, Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", category.getId() == null ? "Thêm danh mục" : "Sửa danh mục");
			return "admin/categories/form";
		}
		this.categoryService.save(category);
		redirectAttributes.addFlashAttribute("successMessage", "Đã lưu danh mục vào cơ sở dữ liệu.");
		return "redirect:/admin/categories";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Long id, Model model) {
		Optional<Category> c = this.categoryService.findById(id);
		if (c.isEmpty()) {
			return "redirect:/admin/categories";
		}
		model.addAttribute("category", c.get());
		model.addAttribute("pageTitle", "Sửa danh mục");
		return "admin/categories/form";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			this.categoryService.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công.");
		} catch (DataIntegrityViolationException ex) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Không xóa được danh mục do có dữ liệu ràng buộc không thể gỡ bỏ. Vui lòng kiểm tra lại.");
		}
		return "redirect:/admin/categories";
	}
}
