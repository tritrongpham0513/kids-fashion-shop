package com.kidfashion.ecommerce.kids_fashion_shop.service;

import com.kidfashion.ecommerce.kids_fashion_shop.repository.ShopOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

	private final ShopOrderRepository shopOrderRepository;

	public StatisticsService(ShopOrderRepository shopOrderRepository) {
		this.shopOrderRepository = shopOrderRepository;
	}

	public Map<String, BigDecimal> revenueLastSixMonths() {
		Map<String, BigDecimal> map = new LinkedHashMap<String, BigDecimal>();
		LocalDate today = LocalDate.now();
		for (int i = 5; i >= 0; i--) {
			YearMonth ym = YearMonth.from(today).minusMonths(i);
			LocalDateTime start = ym.atDay(1).atStartOfDay();
			LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
			BigDecimal rev = this.shopOrderRepository.sumRevenueBetween(start, end);
			String key = ym.getMonthValue() + "/" + ym.getYear();
			map.put(key, rev);
		}
		return map;
	}

	public Map<String, Long> orderCountLastSixMonths() {
		Map<String, Long> map = new LinkedHashMap<String, Long>();
		LocalDate today = LocalDate.now();
		for (int i = 5; i >= 0; i--) {
			YearMonth ym = YearMonth.from(today).minusMonths(i);
			LocalDateTime start = ym.atDay(1).atStartOfDay();
			LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
			long cnt = this.shopOrderRepository.countOrdersBetween(start, end);
			String key = ym.getMonthValue() + "/" + ym.getYear();
			map.put(key, Long.valueOf(cnt));
		}
		return map;
	}

	public List<String> monthKeysLastSix() {
		List<String> keys = new ArrayList<String>();
		LocalDate today = LocalDate.now();
		for (int i = 5; i >= 0; i--) {
			YearMonth ym = YearMonth.from(today).minusMonths(i);
			String key = ym.getMonthValue() + "/" + ym.getYear();
			keys.add(key);
		}
		return keys;
	}
}
