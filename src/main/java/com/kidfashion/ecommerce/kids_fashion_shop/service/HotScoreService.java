package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.OrderLineRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Điểm HOT:
 * - Lượt mua + đánh giá (tự động)
 * - Admin tick HOT: cộng điểm lớn để luôn ưu tiên trên danh sách HOT
 */
@Service
public class HotScoreService {

	private static final double W_SALES = 0.5d;
	private static final double W_REVIEW = 0.5d;
	/** Điểm cộng khi admin bật “Sản phẩm HOT” — vượt xa điểm organic */
	private static final double BOOST_ADMIN_HOT = 1000.0d;

	private final ProductRepository productRepository;
	private final OrderLineRepository orderLineRepository;
	private final ProductReviewRepository productReviewRepository;

	public HotScoreService(ProductRepository productRepository, OrderLineRepository orderLineRepository,
			ProductReviewRepository productReviewRepository) {
		this.productRepository = productRepository;
		this.orderLineRepository = orderLineRepository;
		this.productReviewRepository = productReviewRepository;
	}

	public double computeScore(Product p) {
		if (p == null || p.getId() == null) {
			return 0.0d;
		}
		long salesQty = this.orderLineRepository.sumQuantityByProductId(p.getId());

		Object[] agg = this.productReviewRepository.countAndAvgRatingByProductId(p.getId());
		long revCount = 0L;
		double avgRating = 0.0d;
		if (agg != null && agg.length >= 2 && agg[0] != null) {
			revCount = ((Number) agg[0]).longValue();
		}
		if (agg != null && agg.length >= 2 && agg[1] != null) {
			avgRating = ((Number) agg[1]).doubleValue();
		}

		double salesPart = W_SALES * Math.log1p(salesQty);

		double revPart = 0.0d;
		if (revCount > 0 && avgRating > 0) {
			revPart = W_REVIEW * (avgRating / 5.0d) * Math.log1p(revCount);
		}

		double adminBoost = Boolean.TRUE.equals(p.getAdminHot()) ? BOOST_ADMIN_HOT : 0.0d;

		return salesPart + revPart + adminBoost;
	}

	@Transactional
	public void recalculateAll() {
		for (Product p : this.productRepository.findAll()) {
			if (p.getId() == null) {
				continue;
			}
			double score = computeScore(p);
			p.setHotScore(Double.valueOf(score));
			this.productRepository.save(p);
		}
	}

	@Transactional
	public void recalculateProduct(Long productId) {
		this.productRepository.findById(productId).ifPresent(p -> {
			p.setHotScore(Double.valueOf(computeScore(p)));
			this.productRepository.save(p);
		});
	}
}
