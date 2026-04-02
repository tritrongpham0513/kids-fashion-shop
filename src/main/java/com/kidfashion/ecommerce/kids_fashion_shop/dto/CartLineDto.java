package com.kidfashion.ecommerce.kids_fashion_shop.dto;

import com.kidfashion.ecommerce.kids_fashion_shop.model.Product;

import java.math.BigDecimal;

public class CartLineDto {

	private Product product;
	private Integer quantity;

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

	public BigDecimal getLineSubtotal() {
		if (this.product == null || this.product.getPrice() == null || this.quantity == null) {
			return BigDecimal.ZERO;
		}
		return this.product.getPrice().multiply(new BigDecimal(this.quantity.toString()));
	}
}
