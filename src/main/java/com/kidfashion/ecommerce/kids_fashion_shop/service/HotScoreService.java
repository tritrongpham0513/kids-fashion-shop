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
		return computeScoreCustom(p, salesQty, revCount, avgRating);
	}

	private double computeScoreCustom(Product p, long salesQty, long revCount, double avgRating) {
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
		java.util.List<Product> products = this.productRepository.findAll();
		if (products.isEmpty()) return;

		// 1. Lấy map doanh số theo lô (N+1 -> 1 query)
		java.util.Map<Long, Long> salesMap = this.orderLineRepository.findAllProductQuantitySold().stream()
				.collect(java.util.stream.Collectors.toMap(
						r -> (Long) r[0],
						r -> ((Number) r[1]).longValue(),
						(a, b) -> a));

		// 2. Lấy map đánh giá theo lô (N+1 -> 1 query)
		java.util.Map<Long, Object[]> reviewMap = this.productReviewRepository.findAllProductReviewsAggregated().stream()
				.collect(java.util.stream.Collectors.toMap(
						r -> (Long) r[0],
						r -> new Object[]{r[1], r[2]},
						(a, b) -> a));

		// 3. Tính toán và lưu
		for (Product p : products) {
			if (p.getId() == null) continue;
			long sq = salesMap.getOrDefault(p.getId(), 0L);
			Object[] rev = reviewMap.get(p.getId());
			long rc = 0L;
			double ra = 0.0d;
			if (rev != null) {
				rc = ((Number) rev[0]).longValue();
				ra = ((Number) rev[1]).doubleValue();
			}
			p.setHotScore(Double.valueOf(computeScoreCustom(p, sq, rc, ra)));
		}
		this.productRepository.saveAll(products);
	}

	@Transactional
	public void recalculateProduct(Long productId) {
		this.productRepository.findById(productId).ifPresent(p -> {
			p.setHotScore(Double.valueOf(computeScore(p)));
			this.productRepository.save(p);
		});
	}
}
