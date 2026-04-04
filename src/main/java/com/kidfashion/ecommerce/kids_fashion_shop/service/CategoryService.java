package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Category;
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
	private final jakarta.persistence.EntityManager entityManager;

	public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository, 
	                       jakarta.persistence.EntityManager entityManager) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.entityManager = entityManager;
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
		// 1. Force the database schema to allow NULL (in case Hibernate didn't do it)
		this.productRepository.repairSchemaForNullableCategory();
		
		// 2. Unlink products using Native SQL (bypasses Hibernate's entity state)
		this.productRepository.setCategoryToNullByCategoryId(id);
		
		// 3. Clear persistence context to ensure Hibernate doesn't have stale category references
		this.entityManager.flush();
		this.entityManager.clear();
		
		// 4. Finally delete the category
		this.categoryRepository.deleteById(id);
		this.categoryRepository.flush();
	}

	@Transactional(readOnly = true)
	public long countProductsByCategoryId(Long categoryId) {
		if (categoryId == null) {
			return 0;
		}
		return this.productRepository.countByCategoryId(categoryId);
	}

}
