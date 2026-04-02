package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.OrderLine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

	@Query("select coalesce(sum(ol.quantity), 0) from OrderLine ol where ol.product.id = :productId")
	long sumQuantityByProductId(@Param("productId") Long productId);

	@Query("select ol.product.id, sum(ol.quantity) from OrderLine ol group by ol.product.id order by sum(ol.quantity) desc")
	List<Object[]> findProductIdsByTotalQuantitySold(Pageable pageable);

	@Query(value = "select ol.product.id, sum(ol.quantity) from OrderLine ol group by ol.product.id order by sum(ol.quantity) desc",
			countQuery = "select count(distinct ol.product.id) from OrderLine ol")
	org.springframework.data.domain.Page<Object[]> findProductIdsByTotalQuantitySoldPaged(Pageable pageable);
}
