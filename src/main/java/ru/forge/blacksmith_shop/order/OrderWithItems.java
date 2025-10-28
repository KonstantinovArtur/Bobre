// src/main/java/ru/forge/blacksmith_shop/order/OrderWithItems.java
package ru.forge.blacksmith_shop.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderWithItems {
    private final OrderSummary header;
    private final List<OrderItemRow> items;

    public OrderWithItems(OrderSummary header, List<OrderItemRow> items) {
        this.header = header;
        this.items = items;
    }

    public OrderSummary getHeader() { return header; }
    public List<OrderItemRow> getItems() { return items; }

    public BigDecimal getItemsSum() {
        return items.stream()
                .map(OrderItemRow::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
