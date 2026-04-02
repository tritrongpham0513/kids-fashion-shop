package com.kidfashion.ecommerce.kids_fashion_shop.dto;

import java.math.BigDecimal;

public class ChartBar {

	private String label;
	private BigDecimal amount;
	private int barPercent;
	private long count;
	private int orderBarPercent;

	public ChartBar() {
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public int getBarPercent() {
		return this.barPercent;
	}

	public void setBarPercent(int barPercent) {
		this.barPercent = barPercent;
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public int getOrderBarPercent() {
		return this.orderBarPercent;
	}

	public void setOrderBarPercent(int orderBarPercent) {
		this.orderBarPercent = orderBarPercent;
	}
}
