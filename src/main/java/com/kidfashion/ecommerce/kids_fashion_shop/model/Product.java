package com.kidfashion.ecommerce.kids_fashion_shop.model;

import java.math.BigDecimal;
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

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	@Column(name = "is_new_arrival", nullable = false)
	private Boolean newArrival;

	@Column(name = "is_best_seller", nullable = false)
	private Boolean bestSeller;

	/** Admin đánh dấu HOT — ưu tiên xếp hạng HOT trên shop */
	@Column(name = "is_admin_hot", nullable = false)
	private Boolean adminHot;

	/** Lượt xem trang chi tiết (quan tâm / click) */
	@Column(name = "view_count", nullable = false)
	private Long viewCount;

	/** Mỗi lần sản phẩm xuất hiện trong một trang kết quả tìm kiếm */
	@Column(name = "search_impression_count", nullable = false)
	private Long searchImpressionCount;

	/** Người dùng mở sản phẩm từ trang tìm kiếm (?src=search) */
	@Column(name = "search_click_count", nullable = false)
	private Long searchClickCount;

	/** Điểm HOT tổng hợp (bán + lượt xem + tìm kiếm + review), cập nhật định kỳ */
	@Column(name = "hot_score", nullable = false)
	private Double hotScore;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = true)
	@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
	private Category category;

	@Column(name = "sizes", length = 500)
	private String sizes;

	@Column(name = "colors", length = 500)
	private String colors;

	/** Tổng số lượng sản phẩm đã bán */
	@Column(name = "sold_count", nullable = false)
	private Long soldCount = 0L;

	public Product() {
		this.newArrival = Boolean.FALSE;
		this.bestSeller = Boolean.FALSE;
		this.adminHot = Boolean.FALSE;
		this.viewCount = Long.valueOf(0L);
		this.searchImpressionCount = Long.valueOf(0L);
		this.searchClickCount = Long.valueOf(0L);
		this.hotScore = Double.valueOf(0.0d);
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getStockQuantity() {
		return this.stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public Boolean getNewArrival() {
		return this.newArrival;
	}

	public void setNewArrival(Boolean newArrival) {
		this.newArrival = newArrival;
	}

	public Boolean getBestSeller() {
		return this.bestSeller;
	}

	public void setBestSeller(Boolean bestSeller) {
		this.bestSeller = bestSeller;
	}

	public Boolean getAdminHot() {
		return this.adminHot;
	}

	public void setAdminHot(Boolean adminHot) {
		this.adminHot = adminHot;
	}

	public Long getViewCount() {
		return this.viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

	public Long getSearchImpressionCount() {
		return this.searchImpressionCount;
	}

	public void setSearchImpressionCount(Long searchImpressionCount) {
		this.searchImpressionCount = searchImpressionCount;
	}

	public Long getSearchClickCount() {
		return this.searchClickCount;
	}

	public void setSearchClickCount(Long searchClickCount) {
		this.searchClickCount = searchClickCount;
	}

	public Double getHotScore() {
		return this.hotScore;
	}

	public void setHotScore(Double hotScore) {
		this.hotScore = hotScore;
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getSizes() {
		return sizes;
	}

	public void setSizes(String sizes) {
		this.sizes = sizes;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	public Long getSoldCount() {
		return soldCount;
	}

	public void setSoldCount(Long soldCount) {
		this.soldCount = soldCount == null ? 0L : soldCount;
	}
}
