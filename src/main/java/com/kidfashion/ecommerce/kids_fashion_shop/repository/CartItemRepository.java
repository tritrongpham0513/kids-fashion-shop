package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kidfashion.ecommerce.kids_fashion_shop.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findByUserIdOrderByIdAsc(Long userId);

	Optional<CartItem> findByUserIdAndProductIdAndColorLabelAndSizeLabel(Long userId, Long productId, String colorLabel,
			String sizeLabel);

	void deleteByUserId(Long userId);

	@Modifying
	@Query("delete from CartItem c where c.user.id = :userId and c.id not in :ids")
	void deleteByUserIdAndIdNotIn(@Param("userId") Long userId, @Param("ids") Collection<Long> ids);

	void deleteByProductId(Long productId);
}

