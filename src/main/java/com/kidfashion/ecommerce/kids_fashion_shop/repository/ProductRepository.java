package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Page<Product> findAllByOrderByHotScoreDescIdDesc(Pageable pageable);

	List<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

	List<Product> findByCategoryIdAndIdNotOrderByHotScoreDesc(Long categoryId, Long productId, Pageable pageable);

	long countByCategoryId(Long categoryId);

	List<Product> findByNameContainingIgnoreCase(String keyword);

	List<Product> findByNewArrivalTrueOrderByCreatedAtDesc(Pageable pageable);

	List<Product> findByBestSellerTrueOrderByCreatedAtDesc(Pageable pageable);

	org.springframework.data.domain.Page<Product> findPageByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);
	org.springframework.data.domain.Page<Product> findPageByNameContainingIgnoreCase(String keyword, Pageable pageable);
	org.springframework.data.domain.Page<Product> findPageByNewArrivalTrueOrderByCreatedAtDesc(Pageable pageable);
	org.springframework.data.domain.Page<Product> findPageByBestSellerTrueOrderByCreatedAtDesc(Pageable pageable);

	@Modifying
	@Transactional
	@Query(value = "UPDATE products SET category_id = NULL WHERE category_id = :categoryId", nativeQuery = true)
	void setCategoryToNullByCategoryId(@Param("categoryId") Long categoryId);

	@Modifying
	@Transactional
	@Query(value = "ALTER TABLE products MODIFY category_id BIGINT NULL", nativeQuery = true)
	void repairSchemaForNullableCategory();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product p where p.id = :id")
	Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
