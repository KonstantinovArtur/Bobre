package ru.forge.blacksmith_shop.analytics.dto;

import java.math.BigDecimal;

public class TopProductDto {
    private final String itemName;
    private final long totalSold;       // SUM(oi.quantity)
    private final BigDecimal revenue;   // SUM(oi.quantity*oi.price_per_unit)

    public TopProductDto(String itemName, long totalSold, BigDecimal revenue) {
        this.itemName = itemName;
        this.totalSold = totalSold;
        this.revenue = revenue;
    }
    public String getItemName() { return itemName; }
    public long getTotalSold() { return totalSold; }
    public BigDecimal getRevenue() { return revenue; }
}
