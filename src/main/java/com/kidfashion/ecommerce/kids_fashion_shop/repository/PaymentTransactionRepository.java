package com.kidfashion.ecommerce.kids_fashion_shop.repository;

import com.kidfashion.ecommerce.kids_fashion_shop.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
	List<PaymentTransaction> findByShopOrderIdOrderByCreatedAtDesc(Long shopOrderId);
	List<PaymentTransaction> findByTransactionRef(String transactionRef);
}
