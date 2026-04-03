package com.kidfashion.ecommerce.kids_fashion_shop.dto;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;

import java.math.BigDecimal;

public class CartLineDto {

	private Product product;
	private Integer quantity;
	/** Khóa dòng giỏ (CartLineKey.encode) — dùng trong form cập nhật / xóa */
	private String lineKey;
	private String colorLabel;
	private String sizeLabel;

	public CartLineDto() {
	}

	public CartLineDto(Product product, Integer quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getLineKey() {
		return this.lineKey;
	}

	public void setLineKey(String lineKey) {
		this.lineKey = lineKey;
	}

	public String getColorLabel() {
		return this.colorLabel;
	}

	public void setColorLabel(String colorLabel) {
		this.colorLabel = colorLabel;
	}

	public String getSizeLabel() {
		return this.sizeLabel;
	}

	public void setSizeLabel(String sizeLabel) {
		this.sizeLabel = sizeLabel;
	}

	public BigDecimal getLineSubtotal() {
		if (this.product == null || this.product.getPrice() == null || this.quantity == null) {
			return BigDecimal.ZERO;
		}
		return this.product.getPrice().multiply(new BigDecimal(this.quantity.toString()));
	}
}
