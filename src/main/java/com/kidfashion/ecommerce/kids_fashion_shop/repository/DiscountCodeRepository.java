package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

	Optional<DiscountCode> findByCodeIgnoreCase(String code);
}
