package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.dto.CartLineDto;
import com.kidfashion.ecommerce.kids_fashion_shop.model.AppUser;
import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import com.kidfashion.ecommerce.kids_fashion_shop.model.OrderLine;
import com.kidfashion.ecommerce.kids_fashion_shop.model.OrderStatus;
import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;
import com.kidfashion.ecommerce.kids_fashion_shop.model.ShopOrder;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.ShopOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShopOrderService {

	private final ShopOrderRepository shopOrderRepository;
	private final ProductService productService;
	private final DiscountCodeService discountCodeService;
	private final AppUserService appUserService;
	private final HotScoreService hotScoreService;
	private final ProductSoldCountService productSoldCountService;

	public ShopOrderService(ShopOrderRepository shopOrderRepository, ProductService productService,
			DiscountCodeService discountCodeService, AppUserService appUserService, HotScoreService hotScoreService,
			ProductSoldCountService productSoldCountService) {
		this.shopOrderRepository = shopOrderRepository;
		this.productService = productService;
		this.discountCodeService = discountCodeService;
		this.appUserService = appUserService;
		this.hotScoreService = hotScoreService;
		this.productSoldCountService = productSoldCountService;
	}

	public List<ShopOrder> findAllNewsestFirst() {
		return this.shopOrderRepository.findAllByOrderByCreatedAtDesc();
	}

	public List<ShopOrder> findForCustomer(Long customerId) {
		return this.shopOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
	}

	public Optional<ShopOrder> findById(Long id) {
		return this.shopOrderRepository.findById(id);
	}

	@Transactional
	public ShopOrder placeOrder(Long customerId, List<CartLineDto> cartLines, String discountCodeRaw,
			String shippingAddressRaw, String paymentMethod) {
		Optional<AppUser> customerOpt = this.appUserService.findById(customerId);
		if (customerOpt.isEmpty()) {
			throw new IllegalStateException("Không tìm thấy khách hàng.");
		}
		if (cartLines == null || cartLines.isEmpty()) {
			throw new IllegalStateException("Giỏ hàng trống.");
		}

		AppUser customer = customerOpt.get();
		String shippingAddress = normalizeShippingAddress(shippingAddressRaw);
		if (shippingAddress == null) {
			throw new IllegalStateException("Vui lòng nhập hoặc chọn địa chỉ giao hàng.");
		}

		ShopOrder order = new ShopOrder();
		order.setCustomer(customer);
		order.setPaymentMethod(paymentMethod == null ? "COD" : paymentMethod);
		
		// Luồng thanh toán: Nếu SePay thì Chờ thanh toán, nếu COD thì Chờ xác nhận
		if ("SEPAY".equalsIgnoreCase(order.getPaymentMethod())) {
			order.setStatus(OrderStatus.CHO_THANH_TOAN);
			order.setPaymentStatus("PENDING");
		} else {
			order.setStatus(OrderStatus.CHO_XAC_NHAN);
			order.setPaymentStatus("COD");
		}

		order.setShippingAddress(shippingAddress);
		order.setCreatedAt(LocalDateTime.now());

		BigDecimal subtotal = BigDecimal.ZERO;
		List<OrderLine> lines = new ArrayList<>();

		for (int i = 0; i < cartLines.size(); i++) {
			CartLineDto dto = cartLines.get(i);
			if (dto == null || dto.getProduct() == null || dto.getProduct().getId() == null) {
				continue;
			}
			Long productId = dto.getProduct().getId();
			Integer q = dto.getQuantity();
			if (q == null || q <= 0) {
				continue;
			}
			Optional<Product> pOpt = this.productService.findByIdForUpdate(productId);
			if (pOpt.isEmpty()) {
				continue;
			}
			Product p = pOpt.get();
			int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
			if (stock < q) {
				throw new IllegalStateException("Sản phẩm \"" + p.getName() + "\" không đủ tồn kho.");
			}

			OrderLine line = new OrderLine();
			line.setShopOrder(order);
			line.setProduct(p);
			line.setQuantity(q);
			line.setUnitPrice(p.getPrice());
			line.setColorLabel(dto.getColorLabel() == null ? "" : dto.getColorLabel());
			line.setSizeLabel(dto.getSizeLabel() == null ? "" : dto.getSizeLabel());

			BigDecimal lineTotal = p.getPrice().multiply(new BigDecimal(q.toString()));
			subtotal = subtotal.add(lineTotal);
			lines.add(line);
		}

		if (lines.isEmpty()) {
			throw new IllegalStateException("Giỏ hàng không có dòng hợp lệ.");
		}

		order.setSubtotal(subtotal.setScale(2, java.math.RoundingMode.HALF_UP));

		BigDecimal discountAmount = BigDecimal.ZERO;
		DiscountCode applied = null;
		if (discountCodeRaw != null && discountCodeRaw.trim().length() > 0) {
			Optional<DiscountCode> dcOpt = this.discountCodeService.findActiveByCodeText(discountCodeRaw);
			if (dcOpt.isPresent()) {
				DiscountCode dc = dcOpt.get();
				String err = this.discountCodeService.validateAndExplain(dc);
				if (err == null) {
					discountAmount = this.discountCodeService.computeDiscountAmount(dc, order.getSubtotal());
					applied = dc;
					order.setDiscountCodeText(dc.getCode());
				}
			}
		}

		order.setDiscountAmount(discountAmount);
		BigDecimal total = order.getSubtotal().subtract(discountAmount);
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		}
		order.setTotalAmount(total.setScale(0, java.math.RoundingMode.HALF_UP));

		for (int i = 0; i < lines.size(); i++) {
			OrderLine line = lines.get(i);
			order.getLines().add(line);
		}

		ShopOrder saved = this.shopOrderRepository.save(order);

		for (int i = 0; i < lines.size(); i++) {
			OrderLine line = lines.get(i);
			Product p = line.getProduct();
			int q = line.getQuantity();
			int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
			p.setStockQuantity(stock - q);
			// Lưu tường minh để đảm bảo tồn kho được cập nhật ngay lập tức xuống DB
			this.productService.save(p);
			// Cập nhật số lượng đã bán (soldCount)
			this.productSoldCountService.incrementSoldCount(p.getId(), q);
		}

		if (applied != null) {
			this.discountCodeService.incrementUsedCount(applied);
		}

		this.hotScoreService.recalculateAll();

		return saved;
	}

	private String normalizeShippingAddress(String raw) {
		if (raw == null) {
			return null;
		}
		String value = raw.trim();
		if (value.isEmpty()) {
			return null;
		}
		if (value.length() > 400) {
			throw new IllegalStateException("Địa chỉ giao hàng quá dài.");
		}
		return value;
	}

	@Transactional
	public void updateStatus(Long orderId, OrderStatus newStatus) {
		Optional<ShopOrder> opt = this.shopOrderRepository.findById(orderId);
		if (opt.isEmpty()) return;
		ShopOrder o = opt.get();
		o.setStatus(newStatus);
		this.shopOrderRepository.save(o);
	}

	@Transactional
	public void completePayment(Long orderId, String transactionId, String transferContent) {
		Optional<ShopOrder> opt = this.shopOrderRepository.findById(orderId);
		if (opt.isPresent()) {
			ShopOrder o = opt.get();
			// Chỉ cập nhật nếu đang chờ thanh toán
			if (o.getStatus() == OrderStatus.CHO_THANH_TOAN) {
				// Sau khi thanh toán thành công, chuyển về Chờ xác nhận để Admin kiểm tra và chuẩn bị hàng
				o.setStatus(OrderStatus.CHO_XAC_NHAN);
				o.setPaymentStatus("PAID");
				o.setSepayTransactionId(transactionId);
				o.setSepayTransferContent(transferContent);
				this.shopOrderRepository.save(o);
			}
		}
	}

	@Transactional
	public void saveSepayTransferContent(Long orderId, String content) {
		this.shopOrderRepository.findById(orderId).ifPresent(o -> {
			o.setSepayTransferContent(content);
			this.shopOrderRepository.save(o);
		});
	}

	@Transactional
	public void requestReturn(Long orderId, Long customerId, String reason) {
		Optional<ShopOrder> opt = this.shopOrderRepository.findById(orderId);
		if (opt.isPresent()) {
			ShopOrder o = opt.get();
			// Kiểm tra quyền sở hữu và trạng thái cho phép trả hàng (phải là HOAN_THANH)
			if (o.getCustomer() != null && o.getCustomer().getId().equals(customerId) 
					&& o.getStatus() == OrderStatus.HOAN_THANH) {
				o.setStatus(OrderStatus.TRA_HANG);
				o.setReturnReason(reason);
				this.shopOrderRepository.save(o);
			}
		}
	}
}
