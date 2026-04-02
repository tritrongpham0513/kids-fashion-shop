package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartSessionService {

	public static final String SESSION_CART_MAP = "SESSION_CART_MAP";

	private final ProductRepository productRepository;

	public CartSessionService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@SuppressWarnings("unchecked")
	public Map<Long, Integer> getCartMap(HttpSession session) {
		Object existing = session.getAttribute(SESSION_CART_MAP);
		if (existing instanceof Map) {
			return (Map<Long, Integer>) existing;
		}
		Map<Long, Integer> map = new HashMap<>();
		session.setAttribute(SESSION_CART_MAP, map);
		return map;
	}

	public void addProduct(HttpSession session, Long productId, int quantity) {
		if (productId == null || quantity <= 0) {
			return;
		}
		Optional<Product> productOpt = this.productRepository.findById(productId);
		if (productOpt.isEmpty()) {
			return;
		}
		Product product = productOpt.get();
		int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity().intValue();
		if (stock <= 0) {
			return;
		}
		Map<Long, Integer> map = this.getCartMap(session);
		Integer current = map.get(productId);
		int base = 0;
		if (current != null) {
			base = current;
		}
		int next = Math.min(base + quantity, stock);
		map.put(productId, next);
	}

	public void updateQuantity(HttpSession session, Long productId, int quantity) {
		Map<Long, Integer> map = this.getCartMap(session);
		if (productId == null || quantity <= 0) {
			map.remove(productId);
			return;
		}
		Optional<Product> productOpt = this.productRepository.findById(productId);
		if (productOpt.isEmpty()) {
			map.remove(productId);
			return;
		}
		int stock = productOpt.get().getStockQuantity() == null ? 0 : productOpt.get().getStockQuantity();
		if (stock <= 0) {
			map.remove(productId);
			return;
		}
		map.put(productId, Math.min(quantity, stock));
	}

	public void removeLine(HttpSession session, Long productId) {
		Map<Long, Integer> map = this.getCartMap(session);
		map.remove(productId);
	}

	public void clear(HttpSession session) {
		Map<Long, Integer> map = this.getCartMap(session);
		map.clear();
	}

	public List<CartLineDto> buildLineViews(HttpSession session) {
		Map<Long, Integer> map = this.getCartMap(session);
		List<CartLineDto> list = new ArrayList<CartLineDto>();
		Iterator<Map.Entry<Long, Integer>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, Integer> entry = iterator.next();
			Long productId = entry.getKey();
			Integer qty = entry.getValue();
			if (productId == null || qty == null || qty <= 0) {
				iterator.remove();
				continue;
			}
			Optional<Product> opt = this.productRepository.findById(productId);
			if (opt.isEmpty()) {
				iterator.remove();
				continue;
			}
			Product product = opt.get();
			int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
			if (stock <= 0) {
				iterator.remove();
				continue;
			}
			int effectiveQty = Math.min(qty, stock);
			if (effectiveQty != qty) {
				entry.setValue(effectiveQty);
			}
			CartLineDto line = new CartLineDto();
			line.setProduct(product);
			line.setQuantity(effectiveQty);
			list.add(line);
		}
		return list;
	}

	public BigDecimal computeSubtotal(HttpSession session) {
		List<CartLineDto> lines = this.buildLineViews(session);
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < lines.size(); i++) {
			CartLineDto line = lines.get(i);
			sum = sum.add(line.getLineSubtotal());
		}
		return sum;
	}
}
