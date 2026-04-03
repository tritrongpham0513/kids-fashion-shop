package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.CategoryRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final HotScoreService hotScoreService;

	public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository,
			HotScoreService hotScoreService) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.hotScoreService = hotScoreService;
	}

	@Transactional(readOnly = true)
	public List<Category> findAllSortedByName() {
		List<Category> all = this.categoryRepository.findAll();
		if (all == null || all.isEmpty()) {
			return java.util.Collections.emptyList();
		}
		// Đảm bảo list có thể sort (Spring Data JPA findAll() thường trả về ArrayList mutable, nhưng đề phòng)
		java.util.ArrayList<Category> mutableList = new java.util.ArrayList<>(all);
		mutableList.sort((a, b) -> {
			String nameA = (a == null || a.getName() == null) ? "" : a.getName();
			String nameB = (b == null || b.getName() == null) ? "" : b.getName();
			return nameA.compareToIgnoreCase(nameB);
		});
		return mutableList;
	}

	public Optional<Category> findById(Long id) {
		return this.categoryRepository.findById(id);
	}

	@Transactional
	public Category save(Category category) {
		if (category.getName() != null) {
			category.setName(category.getName().trim());
		}
		if (category.getDescription() != null) {
			category.setDescription(category.getDescription().trim());
		}
		return this.categoryRepository.saveAndFlush(category);
	}

	@Transactional
	public void deleteById(Long id) {
		this.categoryRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public long countProductsByCategoryId(Long categoryId) {
		if (categoryId == null) {
			return 0;
		}
		return this.productRepository.countByCategoryId(categoryId);
	}

	@Transactional
	public void reassignProductsAndDeleteCategory(Long sourceCategoryId, Long targetCategoryId) {
		if (sourceCategoryId == null || targetCategoryId == null) {
			throw new IllegalArgumentException("Thiếu danh mục nguồn/đích.");
		}
		if (sourceCategoryId.equals(targetCategoryId)) {
			throw new IllegalArgumentException("Danh mục đích phải khác danh mục cần xóa.");
		}
		Category source = this.categoryRepository.findById(sourceCategoryId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cần xóa."));
		Category target = this.categoryRepository.findById(targetCategoryId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục đích."));

		List<Product> products = this.productRepository.findByCategoryIdOrderByCreatedAtDesc(sourceCategoryId);
		for (int i = 0; i < products.size(); i++) {
			Product p = products.get(i);
			p.setCategory(target);
			this.productRepository.save(p);
			if (p.getId() != null) {
				this.hotScoreService.recalculateProduct(p.getId());
			}
		}
		this.categoryRepository.delete(source);
	}
}
