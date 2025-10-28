// src/main/java/ru/forge/blacksmith_shop/order/OrderSummary.java
package ru.forge.blacksmith_shop.order;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class OrderSummary {
    private final Integer orderId;
    private final Timestamp orderDate;
    private final String statusTitle;
    private final BigDecimal totalSum;
    private final String addressLine;
    private final BigDecimal shippingCost;

    public OrderSummary(Integer orderId, Timestamp orderDate, String statusTitle,
                        BigDecimal totalSum, String addressLine, BigDecimal shippingCost) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.statusTitle = statusTitle;
        this.totalSum = totalSum;
        this.addressLine = addressLine;
        this.shippingCost = shippingCost;
    }

    public Integer getOrderId() { return orderId; }
    public Timestamp getOrderDate() { return orderDate; }
    public String getStatusTitle() { return statusTitle; }
    public BigDecimal getTotalSum() { return totalSum; }
    public String getAddressLine() { return addressLine; }
    public BigDecimal getShippingCost() { return shippingCost; }
}
