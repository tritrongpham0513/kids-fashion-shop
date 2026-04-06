package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.CartItemRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.OrderLineRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final OrderLineRepository orderLineRepository;
	private final ProductReviewRepository productReviewRepository;
	private final CartItemRepository cartItemRepository;
	private final HotScoreService hotScoreService;

	public ProductService(ProductRepository productRepository, OrderLineRepository orderLineRepository,
			ProductReviewRepository productReviewRepository, CartItemRepository cartItemRepository,
			HotScoreService hotScoreService) {
		this.productRepository = productRepository;
		this.orderLineRepository = orderLineRepository;
		this.productReviewRepository = productReviewRepository;
		this.cartItemRepository = cartItemRepository;
		this.hotScoreService = hotScoreService;
	}

	public Optional<Product> findById(Long id) {
		return this.productRepository.findById(id);
	}

	@Transactional
	public Optional<Product> findByIdForUpdate(Long id) {
		return this.productRepository.findByIdForUpdate(id);
	}

	public List<Product> searchByName(String keyword) {
		if (keyword == null || keyword.trim().length() == 0) {
			return new ArrayList<Product>();
		}
		return this.productRepository.findByNameContainingIgnoreCase(keyword.trim());
	}

	public List<Product> findByCategory(Long categoryId) {
		return this.productRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
	}

	public List<Product> findNewArrivals(int limit) {
		PageRequest page = PageRequest.of(0, limit);
		return this.productRepository.findByNewArrivalTrueOrderByCreatedAtDesc(page);
	}

	/**
	 * Sản phẩm HOT trang chủ: theo {@link HotScoreService} (lượt mua + đánh giá).
	 */
	public List<Product> findBestSellingForHome(int limit) {
		PageRequest page = PageRequest.of(0, limit);
		List<Product> hot = this.productRepository.findAllByOrderByHotScoreDescIdDesc(page).getContent();
		if (hot.isEmpty()) {
			return this.productRepository.findByBestSellerTrueOrderByCreatedAtDesc(page);
		}
		return hot;
	}

	public List<Product> findAll() {
		return this.productRepository.findAll();
	}

	public Page<Product> findAllPaged(int page, int size) {
		return this.productRepository.findAll(PageRequest.of(page, size));
	}

	public Page<Product> searchByNamePaged(String keyword, int page, int size) {
		if (keyword == null || keyword.trim().length() == 0) {
			return new PageImpl<>(new ArrayList<>());
		}
		return this.productRepository.findPageByNameContainingIgnoreCase(keyword.trim(), PageRequest.of(page, size));
	}

	public Page<Product> findByCategoryPaged(Long categoryId, int page, int size) {
		return this.productRepository.findPageByCategoryIdOrderByCreatedAtDesc(categoryId, PageRequest.of(page, size));
	}

	public Page<Product> findNewArrivalsPaged(int page, int size) {
		return this.productRepository.findPageByNewArrivalTrueOrderByCreatedAtDesc(PageRequest.of(page, size));
	}

	/**
	 * Trang "Sản phẩm HOT" (/products/hot): xếp theo điểm HOT (mua + đánh giá).
	 */
	public Page<Product> findBestSellingPaged(int page, int size) {
		PageRequest pageReq = PageRequest.of(page, size);
		Page<Product> hot = this.productRepository.findAllByOrderByHotScoreDescIdDesc(pageReq);
		if (hot.getTotalElements() == 0) {
			return this.productRepository.findPageByBestSellerTrueOrderByCreatedAtDesc(pageReq);
		}
		return hot;
	}

	@Transactional
	public Product save(Product product) {
		Product saved = this.productRepository.save(product);
		if (saved.getId() != null) {
			this.hotScoreService.recalculateProduct(saved.getId());
		}
		return saved;
	}

	@Transactional
	public void repairSchema() {
		this.productRepository.repairSchemaForNullableCategory();
	}

	@Transactional
	public void deleteById(Long id) {
		if (id == null) {
			return;
		}
		if (this.orderLineRepository.existsByProductId(id)) {
			throw new IllegalStateException("Không thể xóa sản phẩm đã phát sinh trong đơn hàng.");
		}
		this.cartItemRepository.deleteByProductId(id);
		this.productReviewRepository.deleteByProductId(id);
		this.productRepository.deleteById(id);
	}

	@Transactional
	public void decreaseStock(Long productId, int quantity) {
		Optional<Product> opt = this.productRepository.findById(productId);
		if (opt.isEmpty()) {
			return;
		}
		Product p = opt.get();
		int current = p.getStockQuantity() == null ? 0 : p.getStockQuantity().intValue();
		int next = current - quantity;
		if (next < 0) {
			next = 0;
		}
		p.setStockQuantity(Integer.valueOf(next));
		this.productRepository.save(p);
	}

	/** Giữ tương thích: logic cũ lấy theo order_lines — chỉ dùng nếu cần báo cáo. */
	public List<Product> findLegacyBestSellersByOrderVolume(int limit) {
		PageRequest page = PageRequest.of(0, limit);
		List<Object[]> rows = this.orderLineRepository.findProductIdsByTotalQuantitySold(page);
		List<Product> result = new ArrayList<Product>();
		if (rows != null && rows.size() > 0) {
			Set<Long> seen = new HashSet<Long>();
			for (int i = 0; i < rows.size(); i++) {
				Object[] row = rows.get(i);
				if (row != null && row.length > 0 && row[0] != null) {
					Long pid = (Long) row[0];
					if (!seen.contains(pid)) {
						seen.add(pid);
						Optional<Product> p = this.productRepository.findById(pid);
						if (p.isPresent()) {
							result.add(p.get());
						}
					}
				}
			}
		}
		return result;
	}

	public long countTotalSold(Long productId) {
		return this.orderLineRepository.sumQuantityByProductId(productId);
	}
}
