package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ProductReview;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductReviewRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.service.CategoryService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ProductService;

@Controller
public class CatalogController {

	private final CategoryService categoryService;
	private final ProductService productService;
	private final ProductReviewRepository productReviewRepository;

	public CatalogController(CategoryService categoryService, ProductService productService,
			ProductReviewRepository productReviewRepository) {
		this.categoryService = categoryService;
		this.productService = productService;
		this.productReviewRepository = productReviewRepository;
	}

	@GetMapping("/categories")
	public String categories(Model model) {
		model.addAttribute("categories", this.categoryService.findAllSortedByName());
		model.addAttribute("pageTitle", "Danh mục");
		return "shop/categories";
	}

	@GetMapping("/categories/{id}")
	public String categoryProducts(@PathVariable("id") Long id,
			@RequestParam(defaultValue = "1") int page, Model model) {
		Optional<Category> cat = this.categoryService.findById(id);
		if (cat.isEmpty()) {
			return "redirect:/categories";
		}
		Page<Product> productPage = this.productService.findByCategoryPaged(id, page - 1, 10);
		model.addAttribute("category", cat.get());
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("pageTitle", cat.get().getName());
		model.addAttribute("requestUrl", "/categories/" + id);
		return "shop/category-products";
	}


	@GetMapping("/products/hot")
	public String hotProducts(@RequestParam(defaultValue = "1") int page, Model model) {
		Page<Product> productPage = this.productService.findBestSellingPaged(page - 1, 10);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("pageTitle", "Sản phẩm HOT");
		model.addAttribute("requestUrl", "/products/hot");
		return "shop/products";
	}

	@GetMapping("/products/new")
	public String newProducts(@RequestParam(defaultValue = "1") int page, Model model) {
		Page<Product> productPage = this.productService.findNewArrivalsPaged(page - 1, 10);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("pageTitle", "Sản phẩm mới");
		model.addAttribute("requestUrl", "/products/new");
		return "shop/products";
	}

	@GetMapping("/products")
	public String allProducts(@RequestParam(defaultValue = "1") int page, Model model) {
		Page<Product> productPage = this.productService.findAllPaged(page - 1, 10);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("pageTitle", "Tất cả sản phẩm");
		model.addAttribute("requestUrl", "/products");
		return "shop/products";
	}

	@GetMapping("/products/{id}")
	public String productDetail(@PathVariable("id") Long id, Model model) {
		Optional<Product> p = this.productService.findById(id);
		if (p.isEmpty()) {
			return "redirect:/";
		}

		List<ProductReview> reviews = this.productReviewRepository.findByProductIdOrderByCreatedAtDesc(id);
		model.addAttribute("productReviews", reviews != null ? reviews : new ArrayList<ProductReview>());
		Object[] agg = this.productReviewRepository.countAndAvgRatingByProductId(id);
		long reviewCount = 0L;
		double reviewAvg = 0.0d;
		if (agg != null && agg.length >= 2) {
			if (agg[0] != null) {
				reviewCount = ((Number) agg[0]).longValue();
			}
			if (agg[1] != null) {
				reviewAvg = ((Number) agg[1]).doubleValue();
			}
		}
		model.addAttribute("reviewCount", Long.valueOf(reviewCount));
		model.addAttribute("reviewAvg", Double.valueOf(reviewAvg));
		model.addAttribute("soldCount", Long.valueOf(this.productService.countTotalSold(id)));

		model.addAttribute("product", p.get());
		model.addAttribute("bestSellingProducts", this.productService.findRelatedProducts(p.get(), 4));
		model.addAttribute("pageTitle", p.get().getName());
		return "shop/product-detail";
	}

	@GetMapping("/search")
	public String search(@RequestParam(name = "q", required = false) String q,
			@RequestParam(defaultValue = "1") int page, Model model) {
		Page<Product> productPage = this.productService.searchByNamePaged(q, page - 1, 10);
		List<Product> content = productPage.getContent();
		model.addAttribute("keyword", q);
		model.addAttribute("products", content);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("pageTitle", "Tìm kiếm");
		return "shop/search";
	}
}
