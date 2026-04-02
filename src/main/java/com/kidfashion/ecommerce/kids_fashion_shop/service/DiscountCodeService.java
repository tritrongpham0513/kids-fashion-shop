package com.kidfashion.ecommerce.kids_fashion_shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kidfashion.ecommerce.kids_fashion_shop.model.DiscountCode;
import com.kidfashion.ecommerce.kids_fashion_shop.repository.DiscountCodeRepository;

@Service
public class DiscountCodeService {

	private final DiscountCodeRepository discountCodeRepository;

	public DiscountCodeService(DiscountCodeRepository discountCodeRepository) {
		this.discountCodeRepository = discountCodeRepository;
	}

	public List<DiscountCode> findAll() {
		return this.discountCodeRepository.findAll();
	}

	public Optional<DiscountCode> findById(Long id) {
		return this.discountCodeRepository.findById(id);
	}

	@Transactional
	public DiscountCode save(DiscountCode code) {
		return this.discountCodeRepository.save(code);
	}

	@Transactional
	public void deleteById(Long id) {
		this.discountCodeRepository.deleteById(id);
	}

	public String validateAndExplain(DiscountCode code) {
		if (code == null) {
			return "Mã khuyến mãi không hợp lệ.";
		}
		if (!Boolean.TRUE.equals(code.getActive())) {
			return "Mã khuyến mãi đã bị vô hiệu hóa.";
		}
		if (code.getExpiryDate() != null && code.getExpiryDate().isBefore(LocalDate.now())) {
			return "Mã khuyến mãi đã hết hạn.";
		}
		if (code.getMaxUses() != null) {
			int used = code.getUsedCount() == null ? 0 : code.getUsedCount().intValue();
			int max = code.getMaxUses().intValue();
			if (used >= max) {
				return "Mã khuyến mãi đã hết lượt sử dụng.";
			}
		}
		boolean hasPercent = code.getPercentOff() != null && code.getPercentOff().compareTo(BigDecimal.ZERO) > 0;
		boolean hasFixed = code.getFixedAmountOff() != null && code.getFixedAmountOff().compareTo(BigDecimal.ZERO) > 0;
		if (!hasPercent && !hasFixed) {
			return "Mã khuyến mãi chưa được cấu hình giảm giá.";
		}
		return null;
	}

	public BigDecimal computeDiscountAmount(DiscountCode code, BigDecimal subtotal) {
		if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		BigDecimal discount = BigDecimal.ZERO;
		if (code.getPercentOff() != null && code.getPercentOff().compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal pct = code.getPercentOff().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
			discount = subtotal.multiply(pct).setScale(2, RoundingMode.HALF_UP);
		}
		if (code.getFixedAmountOff() != null && code.getFixedAmountOff().compareTo(BigDecimal.ZERO) > 0) {
			discount = discount.add(code.getFixedAmountOff());
		}
		if (discount.compareTo(subtotal) > 0) {
			discount = subtotal;
		}
		return discount.setScale(2, RoundingMode.HALF_UP);
	}

	@Transactional
	public void incrementUsedCount(DiscountCode code) {
		int u = code.getUsedCount() == null ? 0 : code.getUsedCount().intValue();
		code.setUsedCount(Integer.valueOf(u + 1));
		this.discountCodeRepository.save(code);
	}

	public Optional<DiscountCode> findActiveByCodeText(String raw) {
		if (raw == null) {
			return Optional.empty();
		}
		String t = raw.trim();
		if (t.length() == 0) {
			return Optional.empty();
		}
		return this.discountCodeRepository.findByCodeIgnoreCase(t);
	}
}
