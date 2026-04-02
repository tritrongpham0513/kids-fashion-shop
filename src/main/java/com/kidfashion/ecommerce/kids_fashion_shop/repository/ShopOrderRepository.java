package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

	List<ShopOrder> findAllByOrderByCreatedAtDesc();

	List<ShopOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

	@Query("select count(o) from ShopOrder o where o.createdAt >= :from and o.createdAt < :to")
	long countOrdersBetween(LocalDateTime from, LocalDateTime to);

	@Query("select coalesce(sum(o.totalAmount), 0) from ShopOrder o where o.createdAt >= :from and o.createdAt < :to")
	BigDecimal sumRevenueBetween(LocalDateTime from, LocalDateTime to);
}
