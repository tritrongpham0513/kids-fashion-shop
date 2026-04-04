package com.kidfashion.ecommerce.kids_fashion_shop.model;

public enum OrderStatus {
	CHO_XAC_NHAN("Chờ xác nhận"),
	DA_THANH_TOAN("Đã thanh toán"),
	DANG_GIAO("Đang giao"),
	HOAN_THANH("Hoàn thành"),
	HUY("Đã hủy");

	private final String displayName;

	OrderStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}
}
