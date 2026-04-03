package com.kidfashion.ecommerce.kids_fashion_shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_cart_user_product_variant", columnNames = { "user_id", "product_id", "color_label",
				"size_label" })
})
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "color_label", nullable = false, length = 120)
	private String colorLabel = "";

	@Column(name = "size_label", nullable = false, length = 120)
	private String sizeLabel = "";

	public CartItem() {
	}

	@PrePersist
	@PreUpdate
	private void normalizeVariantLabels() {
		if (this.colorLabel == null) {
			this.colorLabel = "";
		}
		if (this.sizeLabel == null) {
			this.sizeLabel = "";
		}
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AppUser getUser() {
		return this.user;
	}

	public void setUser(AppUser user) {
		this.user = user;
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

	public String getColorLabel() {
		return this.colorLabel;
	}

	public void setColorLabel(String colorLabel) {
		this.colorLabel = colorLabel == null ? "" : colorLabel;
	}

	public String getSizeLabel() {
		return this.sizeLabel;
	}

	public void setSizeLabel(String sizeLabel) {
		this.sizeLabel = sizeLabel == null ? "" : sizeLabel;
	}
}

