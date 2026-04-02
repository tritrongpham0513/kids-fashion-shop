package com.kidfashion.ecommerce.kids_fashion_shop.dto;

/**
 * Kết quả xem trước mã giảm giá (JSON cho trang thanh toán).
 */
public class CheckoutDiscountPreviewResponse {

	private boolean valid;
	private String message;
	private String subtotalText;
	private String discountText;
	private String totalText;

	public CheckoutDiscountPreviewResponse() {
	}

	public CheckoutDiscountPreviewResponse(boolean valid, String message, String subtotalText, String discountText,
			String totalText) {
		this.valid = valid;
		this.message = message;
		this.subtotalText = subtotalText;
		this.discountText = discountText;
		this.totalText = totalText;
	}

	public boolean isValid() {
		return this.valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubtotalText() {
		return this.subtotalText;
	}

	public void setSubtotalText(String subtotalText) {
		this.subtotalText = subtotalText;
	}

	public String getDiscountText() {
		return this.discountText;
	}

	public void setDiscountText(String discountText) {
		this.discountText = discountText;
	}

	public String getTotalText() {
		return this.totalText;
	}

	public void setTotalText(String totalText) {
		this.totalText = totalText;
	}
}
