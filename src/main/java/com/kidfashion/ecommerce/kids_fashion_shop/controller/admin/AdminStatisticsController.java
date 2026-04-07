package com.kidfashion.ecommerce.kids_fashion_shop.controller.admin;

import com.kidfashion.ecommerce.kids_fashion_shop.dto.ChartBar;
import com.kidfashion.ecommerce.kids_fashion_shop.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Trung tâm thống kê doanh thu và phân tích đơn hàng
@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

	private final StatisticsService statisticsService;

	public AdminStatisticsController(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	@GetMapping("")
	public String statistics(Model model) {
		Map<String, BigDecimal> revenue = this.statisticsService.revenueLastSixMonths();
		Map<String, Long> counts = this.statisticsService.orderCountLastSixMonths();
		List<String> labels = this.statisticsService.monthKeysLastSix();

		BigDecimal maxRevenue = BigDecimal.ZERO;
		for (int i = 0; i < labels.size(); i++) {
			String key = labels.get(i);
			BigDecimal v = revenue.get(key);
			if (v != null && v.compareTo(maxRevenue) > 0) {
				maxRevenue = v;
			}
		}
		if (maxRevenue.compareTo(BigDecimal.ZERO) == 0) {
			maxRevenue = BigDecimal.ONE;
		}

		long maxOrders = 0L;
		for (int i = 0; i < labels.size(); i++) {
			String key = labels.get(i);
			Long c = counts.get(key);
			long cv = 0L;
			if (c != null) {
				cv = c.longValue();
			}
			if (cv > maxOrders) {
				maxOrders = cv;
			}
		}
		if (maxOrders == 0L) {
			maxOrders = 1L;
		}

		List<ChartBar> rows = new ArrayList<ChartBar>();
		for (int i = 0; i < labels.size(); i++) {
			String key = labels.get(i);
			ChartBar row = new ChartBar();
			row.setLabel(key);
			BigDecimal amt = revenue.get(key);
			if (amt == null) {
				amt = BigDecimal.ZERO;
			}
			row.setAmount(amt);
			int pct = amt.multiply(new BigDecimal("100")).divide(maxRevenue, 0, RoundingMode.HALF_UP).intValue();
			row.setBarPercent(pct);

			Long oc = counts.get(key);
			long ocv = 0L;
			if (oc != null) {
				ocv = oc.longValue();
			}
			row.setCount(ocv);
			int opct = (int) ((ocv * 100L) / maxOrders);
			row.setOrderBarPercent(opct);
			rows.add(row);
		}

		model.addAttribute("chartRows", rows);
		model.addAttribute("pageTitle", "Thống kê");
		return "admin/statistics";
	}
}
