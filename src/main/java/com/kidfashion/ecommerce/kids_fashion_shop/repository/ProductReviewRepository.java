package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

	@Query("select count(r), coalesce(avg(r.rating), 0) from ProductReview r where r.product.id = :pid")
	Object[] countAndAvgRatingByProductId(@Param("pid") Long productId);

	Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

	long countByProductId(Long productId);

	List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

	void deleteByProductId(Long productId);
}
