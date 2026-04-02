package com.kidfashion.ecommerce.kids_fashion_shop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ProductReview;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.AppUserRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductReviewRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.service.HotScoreService;
import com.kidfashion.ecommerce.kids_fashion_shop.service.ProductService;

@Controller
public class ProductReviewController {

	private final ProductService productService;
	private final AppUserRepository appUserRepository;
	private final ProductReviewRepository productReviewRepository;
	private final HotScoreService hotScoreService;

	public ProductReviewController(ProductService productService, AppUserRepository appUserRepository,
			ProductReviewRepository productReviewRepository, HotScoreService hotScoreService) {
		this.productService = productService;
		this.appUserRepository = appUserRepository;
		this.productReviewRepository = productReviewRepository;
		this.hotScoreService = hotScoreService;
	}

	@PostMapping("/products/{productId}/reviews")
	@PreAuthorize("hasRole('CUSTOMER')")
	public String addReview(@PathVariable("productId") Long productId, @RequestParam("rating") int rating,
			@RequestParam(value = "comment", required = false) String comment, Principal principal,
			RedirectAttributes redirectAttributes) {
		Optional<Product> pOpt = this.productService.findById(productId);
		if (pOpt.isEmpty()) {
			return "redirect:/";
		}
		if (rating < 1 || rating > 5) {
			redirectAttributes.addFlashAttribute("reviewError", "Điểm đánh giá từ 1 đến 5.");
			return "redirect:/products/" + productId + "#reviews";
		}
		Optional<AppUser> userOpt = this.appUserRepository.findByEmailIgnoreCase(principal.getName());
		if (userOpt.isEmpty()) {
			return "redirect:/login";
		}
		AppUser user = userOpt.get();
		Product product = pOpt.get();

		Optional<ProductReview> existing = this.productReviewRepository.findByProductIdAndUserId(productId, user.getId());
		ProductReview r = existing.orElseGet(ProductReview::new);
		r.setProduct(product);
		r.setUser(user);
		r.setRating(Integer.valueOf(rating));
		if (comment != null && comment.trim().length() > 0) {
			r.setComment(comment.trim());
		} else {
			r.setComment(null);
		}
		if (r.getCreatedAt() == null) {
			r.setCreatedAt(LocalDateTime.now());
		}
		this.productReviewRepository.save(r);

		this.hotScoreService.recalculateProduct(productId);

		redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá.");
		return "redirect:/products/" + productId + "#reviews";
	}
}
