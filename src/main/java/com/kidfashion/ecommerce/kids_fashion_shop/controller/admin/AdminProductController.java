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
import com.kidfashion.ecommerce.kids_fashion_shop.service.FileStorageService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

	private final ProductService productService;
	private final CategoryService categoryService;
	private final FileStorageService fileStorageService;

	public AdminProductController(ProductService productService, 
	                            CategoryService categoryService,
	                            FileStorageService fileStorageService) {
		this.productService = productService;
		this.categoryService = categoryService;
		this.fileStorageService = fileStorageService;
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
	public String save(@ModelAttribute("product") Product product, 
	                   @RequestParam(value = "categoryId", required = false) Long categoryId,
	                   @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
	                   @RequestParam(value = "extraImages", required = false) org.springframework.web.multipart.MultipartFile[] extraImages,
	                   RedirectAttributes redirectAttributes) {
		
		boolean isNew = (product.getId() == null);
		
		// 1. Handle Main Image Upload
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				String imageUrl = this.fileStorageService.saveFile(imageFile);
				product.setImageUrl(imageUrl);
			} catch (java.io.IOException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tải ảnh chính: " + e.getMessage());
				return isNew ? "redirect:/admin/products/new" : "redirect:/admin/products/" + product.getId() + "/edit";
			}
		}

		// 2. Resolve Category
		if (categoryId != null && categoryId > 0) {
			Optional<Category> cat = this.categoryService.findById(categoryId);
			product.setCategory(cat.orElse(null));
		} else {
			product.setCategory(null);
		}
		
		// 3. Sync Existing Data if Editing
		if (!isNew) {
			Optional<Product> existing = this.productService.findById(product.getId());
			if (existing.isPresent()) {
				Product e = existing.get();
				product.setCreatedAt(e.getCreatedAt());
				product.setViewCount(e.getViewCount());
				product.setSearchImpressionCount(e.getSearchImpressionCount());
				product.setSearchClickCount(e.getSearchClickCount());
				product.setHotScore(e.getHotScore());
				product.setImages(e.getImages()); // Keep existing images
				
				if ((product.getImageUrl() == null || product.getImageUrl().isBlank()) && e.getImageUrl() != null) {
					product.setImageUrl(e.getImageUrl());
				}
			}
		} else {
			product.setCreatedAt(LocalDateTime.now());
		}
		
		// 4. Default Boolean Flags
		if (product.getNewArrival() == null) product.setNewArrival(Boolean.FALSE);
		if (product.getBestSeller() == null) product.setBestSeller(Boolean.FALSE);
		if (product.getAdminHot() == null) product.setAdminHot(Boolean.FALSE);
		
		// 5. Save Product First
		Product savedProduct = this.productService.save(product);

		// 6. Handle Extra Gallery Images
		if (extraImages != null && extraImages.length > 0) {
			for (org.springframework.web.multipart.MultipartFile file : extraImages) {
				if (file != null && !file.isEmpty()) {
					try {
						String url = this.fileStorageService.saveFile(file);
						com.kidfashion.ecommerce.kids_fashion_shop.model.ProductImage pi = new com.kidfashion.ecommerce.kids_fashion_shop.model.ProductImage();
						pi.setProduct(savedProduct);
						pi.setImageUrl(url);
						pi.setSortOrder(0);
						savedProduct.getImages().add(pi);
					} catch (java.io.IOException e) {
						// Log error but continue with other images
					}
				}
			}
			this.productService.save(savedProduct);
		}
		
		String msg = isNew ? "Đã thêm sản phẩm mới!" : "Đã cập nhật sản phẩm thành công!";
		redirectAttributes.addFlashAttribute("successMessage", msg);
		return "redirect:/admin/products";
	}

	@PostMapping("/images/{imageId}/delete")
	public String deleteImage(@PathVariable("imageId") Long imageId, 
	                        @RequestParam("productId") Long productId,
	                        RedirectAttributes redirectAttributes) {
		Optional<Product> pOpt = this.productService.findById(productId);
		if (pOpt.isPresent()) {
			Product p = pOpt.get();
			p.getImages().removeIf(img -> img.getId().equals(imageId));
			this.productService.save(p);
			redirectAttributes.addFlashAttribute("successMessage", "Đã xóa ảnh khỏi album.");
		}
		return "redirect:/admin/products/" + productId + "/edit";
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
					"Không thể xóa sản phẩm vì còn dữ liệu liên quan.");
		}
		return "redirect:/admin/products";
	}
}
