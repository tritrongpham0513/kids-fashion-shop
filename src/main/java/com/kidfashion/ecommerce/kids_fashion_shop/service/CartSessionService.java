package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.dto.BuyNowCheckoutSession;
import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.model.CartLineKey;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CartSessionService {

	/** Giỏ theo phiên: key = CartLineKey.encode(...), value = số lượng */
	public static final String SESSION_CART_LINES = "SESSION_CART_LINES";

	/** Cũ: Map<Long, Integer> productId → qty — tự migrate sang SESSION_CART_LINES */
	public static final String SESSION_CART_MAP = "SESSION_CART_MAP";

	/** Mua ngay: thanh toán một dòng, không gộp giỏ thường */
	public static final String SESSION_BUY_NOW_CHECKOUT = "SESSION_BUY_NOW_CHECKOUT";

	/** Chọn nhiều dòng trong giỏ để thanh toán */
	public static final String SESSION_CART_SELECTED_KEYS = "SESSION_CART_SELECTED_KEYS";

	private final ProductRepository productRepository;

	public CartSessionService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Integer> getCartLineMap(HttpSession session) {
		migrateLegacyCartIfNeeded(session);
		Object existing = session.getAttribute(SESSION_CART_LINES);
		Map<String, Integer> map;
		if (existing instanceof Map) {
			map = (Map<String, Integer>) existing;
		} else {
			map = new HashMap<>();
			session.setAttribute(SESSION_CART_LINES, map);
		}
		CartLineKey.rekeyLegacyRows(map);
		return map;
	}

	@SuppressWarnings("unchecked")
	private void migrateLegacyCartIfNeeded(HttpSession session) {
		if (session.getAttribute(SESSION_CART_LINES) != null) {
			return;
		}
		Object old = session.getAttribute(SESSION_CART_MAP);
		Map<String, Integer> nu = new HashMap<>();
		if (old instanceof Map<?, ?> om) {
			for (Map.Entry<?, ?> e : om.entrySet()) {
				if (e.getKey() instanceof Long pid && e.getValue() instanceof Integer qty) {
					nu.put(CartLineKey.encode(pid.longValue(), "", ""), qty);
				}
			}
			session.removeAttribute(SESSION_CART_MAP);
		}
		session.setAttribute(SESSION_CART_LINES, nu);
	}

	public void addProduct(HttpSession session, Long productId, int quantity, String colorLabel, String sizeLabel) {
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
		String key = CartLineKey.encode(productId, colorLabel, sizeLabel);
		Map<String, Integer> map = getCartLineMap(session);
		Integer current = map.get(key);
		int base = current == null ? 0 : current;
		int next = Math.min(base + quantity, stock);
		map.put(key, next);
	}

	public void updateQuantity(HttpSession session, String lineKey, int quantity) {
		Map<String, Integer> map = getCartLineMap(session);
		if (lineKey == null || lineKey.isBlank()) {
			return;
		}
		CartLineKey.Parsed p;
		try {
			p = CartLineKey.parse(lineKey);
		} catch (Exception ex) {
			return;
		}
		String canonical = CartLineKey.encode(p.productId, p.colorLabel, p.sizeLabel);
		if (quantity <= 0) {
			map.remove(canonical);
			return;
		}
		Optional<Product> productOpt = this.productRepository.findById(p.productId);
		if (productOpt.isEmpty()) {
			map.remove(canonical);
			return;
		}
		int stock = productOpt.get().getStockQuantity() == null ? 0 : productOpt.get().getStockQuantity();
		if (stock <= 0) {
			map.remove(canonical);
			return;
		}
		map.put(canonical, Math.min(quantity, stock));
	}

	public void removeLine(HttpSession session, String lineKey) {
		if (lineKey == null || lineKey.isBlank()) {
			return;
		}
		Map<String, Integer> map = getCartLineMap(session);
		try {
			CartLineKey.Parsed p = CartLineKey.parse(lineKey);
			map.remove(CartLineKey.encode(p.productId, p.colorLabel, p.sizeLabel));
		} catch (Exception ex) {
			map.remove(lineKey);
		}
	}

	public void clear(HttpSession session) {
		getCartLineMap(session).clear();
	}

	// ------------------- BUY NOW -------------------

	public void setBuyNowCheckout(HttpSession session, long productId, int quantity, String colorLabel, String sizeLabel) {
		int q = quantity <= 0 ? 1 : quantity;
		session.setAttribute(SESSION_BUY_NOW_CHECKOUT,
				new BuyNowCheckoutSession(productId, q, colorLabel, sizeLabel));
	}

	public void clearBuyNowCheckout(HttpSession session) {
		session.removeAttribute(SESSION_BUY_NOW_CHECKOUT);
	}

	/**
	 * Dòng hiển thị / đặt hàng cho chế độ Mua ngay. Hết hàng hoặc không còn SP → xóa session và trả rỗng.
	 */
	public Optional<CartLineDto> buildBuyNowLineView(HttpSession session) {
		Object raw = session.getAttribute(SESSION_BUY_NOW_CHECKOUT);
		if (!(raw instanceof BuyNowCheckoutSession b)) {
			return Optional.empty();
		}
		Optional<Product> pOpt = this.productRepository.findById(b.getProductId());
		if (pOpt.isEmpty()) {
			clearBuyNowCheckout(session);
			return Optional.empty();
		}
		Product product = pOpt.get();
		int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
		if (stock <= 0) {
			clearBuyNowCheckout(session);
			return Optional.empty();
		}
		int qty = Math.min(Math.max(1, b.getQuantity()), stock);
		CartLineDto line = new CartLineDto();
		line.setProduct(product);
		line.setQuantity(qty);
		line.setColorLabel(b.getColorLabel());
		line.setSizeLabel(b.getSizeLabel());
		line.setLineKey(CartLineKey.encode(b.getProductId(), b.getColorLabel(), b.getSizeLabel()));
		return Optional.of(line);
	}

	// ------------------- CHOOSE LINES -------------------

	public void setSelectedCheckoutKeys(HttpSession session, List<String> selectedLineKeys) {
		Set<String> out = new HashSet<>();
		if (selectedLineKeys != null) {
			for (String k : selectedLineKeys) {
				if (k == null || k.isBlank()) {
					continue;
				}
				try {
					CartLineKey.Parsed p = CartLineKey.parse(k);
					out.add(CartLineKey.encode(p.productId, p.colorLabel, p.sizeLabel));
				} catch (Exception ignored) {
				}
			}
		}
		session.setAttribute(SESSION_CART_SELECTED_KEYS, out);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getSelectedCheckoutKeys(HttpSession session) {
		Object raw = session.getAttribute(SESSION_CART_SELECTED_KEYS);
		if (raw instanceof Set<?> s) {
			Set<String> out = new HashSet<>();
			for (Object item : s) {
				if (item != null) {
					out.add(item.toString());
				}
			}
			return out;
		}
		return Collections.emptySet();
	}

	public void clearSelectedCheckoutKeys(HttpSession session) {
		session.removeAttribute(SESSION_CART_SELECTED_KEYS);
	}

	public List<CartLineDto> buildSelectedLineViews(HttpSession session) {
		Set<String> keys = getSelectedCheckoutKeys(session);
		if (keys.isEmpty()) {
			return List.of();
		}
		List<CartLineDto> all = buildLineViews(session);
		List<CartLineDto> out = new ArrayList<>();
		for (int i = 0; i < all.size(); i++) {
			CartLineDto l = all.get(i);
			if (keys.contains(l.getLineKey())) {
				out.add(l);
			}
		}
		return out;
	}

	public void removeSelectedLinesFromCart(HttpSession session) {
		Set<String> keys = getSelectedCheckoutKeys(session);
		if (keys.isEmpty()) {
			return;
		}
		for (String k : keys) {
			removeLine(session, k);
		}
		clearSelectedCheckoutKeys(session);
	}

	// ------------------- CART LIST -------------------

	public List<CartLineDto> buildLineViews(HttpSession session) {
		Map<String, Integer> map = getCartLineMap(session);
		List<CartLineDto> list = new ArrayList<>();
		Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Integer> entry = iterator.next();
			String lineKey = entry.getKey();
			Integer qty = entry.getValue();
			if (lineKey == null || qty == null || qty <= 0) {
				iterator.remove();
				continue;
			}
			CartLineKey.Parsed p;
			try {
				p = CartLineKey.parse(lineKey);
			} catch (Exception ex) {
				iterator.remove();
				continue;
			}
			Optional<Product> opt = this.productRepository.findById(p.productId);
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
			line.setLineKey(lineKey);
			line.setColorLabel(p.colorLabel);
			line.setSizeLabel(p.sizeLabel);
			list.add(line);
		}
		return list;
	}

	public BigDecimal computeSubtotal(HttpSession session) {
		List<CartLineDto> lines = buildLineViews(session);
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < lines.size(); i++) {
			sum = sum.add(lines.get(i).getLineSubtotal());
		}
		return sum;
	}

	public BigDecimal computeSubtotalSelected(HttpSession session) {
		List<CartLineDto> lines = buildSelectedLineViews(session);
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < lines.size(); i++) {
			sum = sum.add(lines.get(i).getLineSubtotal());
		}
		return sum;
	}
}

