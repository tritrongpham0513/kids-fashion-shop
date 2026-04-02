package com.kidfashion.ecommerce.kids_fashion_shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "discount_codes")
public class DiscountCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 60)
	private String code;

	@Column(name = "percent_off", precision = 5, scale = 2)
	private BigDecimal percentOff;

	@Column(name = "fixed_amount_off", precision = 12, scale = 2)
	private BigDecimal fixedAmountOff;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	@Column(nullable = false)
	private Boolean active;

	@Column(name = "max_uses")
	private Integer maxUses;

	@Column(name = "used_count", nullable = false)
	private Integer usedCount;

	public DiscountCode() {
		this.active = Boolean.TRUE;
		this.usedCount = Integer.valueOf(0);
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getPercentOff() {
		return this.percentOff;
	}

	public void setPercentOff(BigDecimal percentOff) {
		this.percentOff = percentOff;
	}

	public BigDecimal getFixedAmountOff() {
		return this.fixedAmountOff;
	}

	public void setFixedAmountOff(BigDecimal fixedAmountOff) {
		this.fixedAmountOff = fixedAmountOff;
	}

	public LocalDate getExpiryDate() {
		return this.expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Boolean getActive() {
		return this.active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Integer getMaxUses() {
		return this.maxUses;
	}

	public void setMaxUses(Integer maxUses) {
		this.maxUses = maxUses;
	}

	public Integer getUsedCount() {
		return this.usedCount;
	}

	public void setUsedCount(Integer usedCount) {
		this.usedCount = usedCount;
	}
}
