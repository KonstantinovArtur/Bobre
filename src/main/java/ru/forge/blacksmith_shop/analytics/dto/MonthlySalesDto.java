package ru.forge.blacksmith_shop.analytics.dto;

import java.math.BigDecimal;

public class MonthlySalesDto {
    private final String month;     // YYYY-MM
    private final BigDecimal total; // сумма по orders.total_sum

    public MonthlySalesDto(String month, BigDecimal total) {
        this.month = month;
        this.total = total;
    }
    public String getMonth() { return month; }
    public BigDecimal getTotal() { return total; }
}
