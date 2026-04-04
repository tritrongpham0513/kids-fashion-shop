package com.kidfashion.ecommerce.kids_fashion_shop.dto;

import java.math.BigDecimal;

public class CartUpdateResponse {
    private boolean success;
    private String message;
    private int updatedQuantity;
    private BigDecimal lineSubtotal;
    private BigDecimal cartSubtotal;
    private int cartItemsCount;

    public CartUpdateResponse() {}

    public CartUpdateResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getUpdatedQuantity() { return updatedQuantity; }
    public void setUpdatedQuantity(int updatedQuantity) { this.updatedQuantity = updatedQuantity; }

    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public void setLineSubtotal(BigDecimal lineSubtotal) { this.lineSubtotal = lineSubtotal; }

    public BigDecimal getCartSubtotal() { return cartSubtotal; }
    public void setCartSubtotal(BigDecimal cartSubtotal) { this.cartSubtotal = cartSubtotal; }

    public int getCartItemsCount() { return cartItemsCount; }
    public void setCartItemsCount(int cartItemsCount) { this.cartItemsCount = cartItemsCount; }
}
