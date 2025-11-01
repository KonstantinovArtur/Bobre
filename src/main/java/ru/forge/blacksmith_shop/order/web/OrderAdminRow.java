package ru.forge.blacksmith_shop.order.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderAdminRow(
        int orderId,
        String idOrder,
        LocalDateTime orderDate,
        int userId,
        String userLogin,
        BigDecimal total,
        String addressLine,
        BigDecimal shippingCost,
        String statusCode,
        String statusTitle,
        Integer itemsCount
) {}
