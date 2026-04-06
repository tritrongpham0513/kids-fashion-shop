package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
	List<ProductImage> findByProductIdAndPrimaryTrue(Long productId);
}
