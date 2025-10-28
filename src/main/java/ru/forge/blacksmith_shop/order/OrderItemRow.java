// src/main/java/ru/forge/blacksmith_shop/order/OrderItemRow.java
package ru.forge.blacksmith_shop.order;

import java.math.BigDecimal;

public class OrderItemRow {
    private final Integer productId;
    private final String name;
    private final int quantity;
    private final BigDecimal pricePerUnit;
    private final BigDecimal lineTotal;

    public OrderItemRow(Integer productId, String name, int quantity, BigDecimal pricePerUnit, BigDecimal lineTotal) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.lineTotal = lineTotal;
    }

    public Integer getProductId() { return productId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
