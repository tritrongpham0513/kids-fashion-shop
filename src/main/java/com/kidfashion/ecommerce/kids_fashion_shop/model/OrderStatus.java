package com.kidfashion.ecommerce.kids_fashion_shop.model;

public enum OrderStatus {
	CHO_THANH_TOAN("Chờ thanh toán"),
	CHO_XAC_NHAN("Chờ xác nhận"),
	DANG_XU_LY("Đang chuẩn bị hàng"),
	DANG_GIAO("Đang giao"),
	HOAN_THANH("Hoàn thành"),
	HUY("Đã hủy"),
	TRA_HANG("Trả hàng/Hoàn tiền");

	private final String displayName;

	OrderStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}
}
