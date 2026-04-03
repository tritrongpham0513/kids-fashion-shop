package com.kidfashion.ecommerce.kids_fashion_shop.dto;

import java.io.Serializable;

/**
 * Phiên thanh toán "Mua ngay" — chỉ một dòng, tách khỏi giỏ hàng thường.
 */
public final class BuyNowCheckoutSession implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long productId;
	private final int quantity;
	private final String colorLabel;
	private final String sizeLabel;

	public BuyNowCheckoutSession(long productId, int quantity, String colorLabel, String sizeLabel) {
		this.productId = productId;
		this.quantity = quantity;
		this.colorLabel = colorLabel == null ? "" : colorLabel;
		this.sizeLabel = sizeLabel == null ? "" : sizeLabel;
	}

	public long getProductId() {
		return this.productId;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public String getColorLabel() {
		return this.colorLabel;
	}

	public String getSizeLabel() {
		return this.sizeLabel;
	}
}
