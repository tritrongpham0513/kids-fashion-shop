package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;
import java.util.Collections;

@ControllerAdvice
public class GlobalModelAdvice {

	private final CategoryService categoryService;

	public GlobalModelAdvice(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	/** Mỗi request đọc lại từ MySQL (không cache) — phản ánh ngay sau khi admin lưu danh mục. */
	@ModelAttribute("navCategories")
	public List<Category> loadNavCategories() {
		try {
			return this.categoryService.findAllSortedByName();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Trạng thái mục điều hướng đang active (theo path), dùng trong layout.
	 */
	@ModelAttribute("navActive")
	public String navActive(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (path == null) {
			return "";
		}
		String ctx = request.getContextPath();
		if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
			path = path.substring(ctx.length());
		}
		if (path.isEmpty() || "/".equals(path)) {
			return "home";
		}
		if (path.startsWith("/products/new")) {
			return "new";
		}
		if (path.startsWith("/products/hot")) {
			return "hot";
		}
		if (path.equals("/products") || path.equals("/products/") || path.startsWith("/products?")) {
			return "products";
		}
		if (path.startsWith("/products/")) {
			return "product";
		}
		if (path.startsWith("/categories")) {
			return "categories";
		}
		if (path.startsWith("/search")) {
			return "search";
		}
		if (path.startsWith("/cart")) {
			return "cart";
		}
		return "";
	}

	/**
	 * ID danh mục đang xem (URL /categories/{id}), dùng highlight ở thanh danh mục nhanh.
	 */
	@ModelAttribute("activeCategoryId")
	public Long activeCategoryId(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (path == null) {
			return null;
		}
		String ctx = request.getContextPath();
		if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
			path = path.substring(ctx.length());
		}
		if (!path.startsWith("/categories/")) {
			return null;
		}
		String rest = path.substring("/categories/".length());
		int slash = rest.indexOf('/');
		String idPart = slash >= 0 ? rest.substring(0, slash) : rest;
		if (idPart.isEmpty()) {
			return null;
		}
		try {
			return Long.parseLong(idPart);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
