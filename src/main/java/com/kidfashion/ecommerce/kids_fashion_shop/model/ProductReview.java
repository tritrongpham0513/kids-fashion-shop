package com.kidfashion.ecommerce.kids_fashion_shop.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "product_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_product_user", columnNames = {
		"product_id", "user_id" }))
public class ProductReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/**
	 * Nếu {@code user_id} trỏ tới user không tồn tại (dữ liệu cũ / lỗi seed), Hibernate trả {@code null}
	 * thay vì ném {@link jakarta.persistence.EntityNotFoundException} khi render template.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@NotFound(action = NotFoundAction.IGNORE)
	private AppUser user;

	@Column(nullable = false)
	private Integer rating;

	@Column(length = 2000)
	private String comment;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public ProductReview() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public AppUser getUser() {
		return this.user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

	public Integer getRating() {
		return this.rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
