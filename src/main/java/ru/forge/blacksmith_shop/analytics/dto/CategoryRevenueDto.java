package ru.forge.blacksmith_shop.analytics.dto;

import java.math.BigDecimal;

public class CategoryRevenueDto {
    private final String category;
    private final BigDecimal revenue;

    public CategoryRevenueDto(String category, BigDecimal revenue) {
        this.category = category;
        this.revenue = revenue;
    }
    public String getCategory() { return category; }
    public BigDecimal getRevenue() { return revenue; }
}
