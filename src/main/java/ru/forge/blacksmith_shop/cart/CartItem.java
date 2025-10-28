// src/main/java/ru/forge/blacksmith_shop/cart/CartItem.java
package ru.forge.blacksmith_shop.cart;

import java.math.BigDecimal;

public class CartItem {
    private Integer productId;
    private String name;
    private BigDecimal priceFinal; // цена на момент добавления
    private int qty;

    public CartItem(Integer productId, String name, BigDecimal priceFinal, int qty) {
        this.productId = productId;
        this.name = name;
        this.priceFinal = priceFinal;
        this.qty = qty;
    }

    public Integer getProductId() { return productId; }
    public String getName() { return name; }
    public BigDecimal getPriceFinal() { return priceFinal; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public BigDecimal getLineTotal() {
        return priceFinal.multiply(BigDecimal.valueOf(qty));
    }

}
