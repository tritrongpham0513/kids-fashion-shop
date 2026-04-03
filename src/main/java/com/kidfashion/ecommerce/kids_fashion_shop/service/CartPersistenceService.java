package com.kidfashion.ecommerce.kids_fashion_shop.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.CartItem;
import com.kidfashion.ecommerce.kids_fashion_shop.model.CartLineKey;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.CartItemRepository;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CartPersistenceService {

	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final AppUserService appUserService;
	private final CartSessionService cartSessionService;

	public CartPersistenceService(CartItemRepository cartItemRepository, ProductRepository productRepository,
			AppUserService appUserService, CartSessionService cartSessionService) {
		this.cartItemRepository = cartItemRepository;
		this.productRepository = productRepository;
		this.appUserService = appUserService;
		this.cartSessionService = cartSessionService;
	}

	@Transactional
	public void mergeSessionCartIntoUserCart(HttpSession session, Long userId) {
		if (session == null || userId == null) {
			return;
		}
		Map<String, Integer> sessionMap = this.cartSessionService.getCartLineMap(session);
		if (sessionMap == null || sessionMap.isEmpty()) {
			loadUserCartIntoSession(session, userId);
			return;
		}

		AppUser user = this.appUserService.findById(userId).orElse(null);
		if (user == null) {
			return;
		}

		for (Map.Entry<String, Integer> e : sessionMap.entrySet()) {
			String lineKey = e.getKey();
			Integer qty = e.getValue();
			if (lineKey == null || qty == null || qty <= 0) {
				continue;
			}
			CartLineKey.Parsed p;
			try {
				p = CartLineKey.parse(lineKey);
			} catch (Exception ex) {
				continue;
			}
			Optional<Product> pOpt = this.productRepository.findById(p.productId);
			if (pOpt.isEmpty()) {
				continue;
			}
			Product prod = pOpt.get();
			int stock = prod.getStockQuantity() == null ? 0 : prod.getStockQuantity().intValue();
			int safeQty = Math.min(qty, stock);
			if (safeQty <= 0) {
				continue;
			}

			String c = p.colorLabel == null ? "" : p.colorLabel;
			String s = p.sizeLabel == null ? "" : p.sizeLabel;
			CartItem item = this.cartItemRepository
					.findByUserIdAndProductIdAndColorLabelAndSizeLabel(userId, p.productId, c, s).orElseGet(() -> {
						CartItem ci = new CartItem();
						ci.setUser(user);
						ci.setProduct(prod);
						ci.setQuantity(0);
						ci.setColorLabel(c);
						ci.setSizeLabel(s);
						return ci;
					});
			int current = item.getQuantity() == null ? 0 : item.getQuantity();
			int merged = Math.min(current + safeQty, stock);
			item.setQuantity(merged);
			item.setColorLabel(c);
			item.setSizeLabel(s);
			this.cartItemRepository.saveAndFlush(item);
		}

		loadUserCartIntoSession(session, userId);
	}

	@Transactional(readOnly = true)
	public void loadUserCartIntoSession(HttpSession session, Long userId) {
		if (session == null || userId == null) {
			return;
		}
		List<CartItem> items = this.cartItemRepository.findByUserIdOrderByIdAsc(userId);
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < items.size(); i++) {
			CartItem ci = items.get(i);
			if (ci.getProduct() == null || ci.getProduct().getId() == null) {
				continue;
			}
			Integer qty = ci.getQuantity();
			if (qty == null || qty <= 0) {
				continue;
			}
			String c = ci.getColorLabel() == null ? "" : ci.getColorLabel();
			String sz = ci.getSizeLabel() == null ? "" : ci.getSizeLabel();
			String key = CartLineKey.encode(ci.getProduct().getId(), c, sz);
			map.put(key, qty);
		}
		session.setAttribute(CartSessionService.SESSION_CART_LINES, map);
		session.removeAttribute(CartSessionService.SESSION_CART_MAP);
	}

	@Transactional
	public void syncUserCartFromSession(HttpSession session, Long userId) {
		if (session == null || userId == null) {
			return;
		}
		AppUser user = this.appUserService.findById(userId).orElse(null);
		if (user == null) {
			return;
		}
		Map<String, Integer> map = this.cartSessionService.getCartLineMap(session);
		if (map == null || map.isEmpty()) {
			this.cartItemRepository.deleteByUserId(userId);
			return;
		}

		Set<Long> keptIds = new HashSet<>();
		for (Map.Entry<String, Integer> e : map.entrySet()) {
			String lineKey = e.getKey();
			Integer qty = e.getValue();
			if (lineKey == null || qty == null || qty <= 0) {
				continue;
			}
			CartLineKey.Parsed p;
			try {
				p = CartLineKey.parse(lineKey);
			} catch (Exception ex) {
				continue;
			}
			Optional<Product> pOpt = this.productRepository.findById(p.productId);
			if (pOpt.isEmpty()) {
				continue;
			}
			Product prod = pOpt.get();
			int stock = prod.getStockQuantity() == null ? 0 : prod.getStockQuantity().intValue();
			int safeQty = Math.min(qty, stock);
			if (safeQty <= 0) {
				continue;
			}
			String c = p.colorLabel == null ? "" : p.colorLabel;
			String sz = p.sizeLabel == null ? "" : p.sizeLabel;
			CartItem item = this.cartItemRepository
					.findByUserIdAndProductIdAndColorLabelAndSizeLabel(userId, p.productId, c, sz).orElseGet(() -> {
						CartItem ci = new CartItem();
						ci.setUser(user);
						ci.setProduct(prod);
						ci.setColorLabel(c);
						ci.setSizeLabel(sz);
						return ci;
					});
			item.setQuantity(safeQty);
			item.setColorLabel(c);
			item.setSizeLabel(sz);
			CartItem saved = this.cartItemRepository.saveAndFlush(item);
			if (saved.getId() != null) {
				keptIds.add(saved.getId());
			}
		}

		if (keptIds.isEmpty()) {
			this.cartItemRepository.deleteByUserId(userId);
		} else {
			this.cartItemRepository.deleteByUserIdAndIdNotIn(userId, keptIds);
		}
	}

	@Transactional
	public void clearUserCart(Long userId) {
		if (userId == null) {
			return;
		}
		this.cartItemRepository.deleteByUserId(userId);
	}
}
