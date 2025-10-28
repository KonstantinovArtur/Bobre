package ru.forge.blacksmith_shop.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class OrderDao {

    private final JdbcTemplate jdbc;

    public OrderDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // вызов вашей функции create_order_from_cart(p_user_id, p_address_line, p_shipping_cost)
    public CreatedOrder createFromCart(int userId, String addressLine, BigDecimal shippingCost) {
        return jdbc.queryForObject(
                "select order_id, total_sum from create_order_from_cart(?, ?, ?)",
                (rs, rn) -> new CreatedOrder(rs.getInt("order_id"), rs.getBigDecimal("total_sum")),
                userId, addressLine, shippingCost == null ? BigDecimal.ZERO : shippingCost
        );
    }

    // берём сводку по заказам пользователя из view
    public List<OrderSummary> findSummariesByUser(int userId) {
        String sql = """
            select s.order_id,
                   s.order_date,
                   s.status_title,
                   s.status_code,
                   s.items_count,
                   s.items_sum,
                   s.shipping_cost,
                   s.computed_total_sum,
                   s.address_line
            from vw_orders_summary s
            join orders o on o.order_id = s.order_id
            where o.user_id = ?
            order by s.order_date desc
            """;
        return jdbc.query(sql, new SummaryMapper(), userId);
    }

    // --- DTO/mapper ---

    public static class CreatedOrder {
        private final int orderId;
        private final BigDecimal total;

        public CreatedOrder(int orderId, BigDecimal total) {
            this.orderId = orderId;
            this.total = total;
        }
        public int getOrderId() { return orderId; }
        public BigDecimal getTotal() { return total; }
    }

    public static class OrderSummary {
        private int orderId;
        private OffsetDateTime orderDate;
        private String statusTitle;
        private String statusCode;
        private int itemsCount;
        private BigDecimal itemsSum;
        private BigDecimal shippingCost;
        private BigDecimal total;
        private String addressLine;

        // getters
        public int getOrderId() { return orderId; }
        public OffsetDateTime getOrderDate() { return orderDate; }
        public String getStatusTitle() { return statusTitle; }
        public String getStatusCode() { return statusCode; }
        public int getItemsCount() { return itemsCount; }
        public BigDecimal getItemsSum() { return itemsSum; }
        public BigDecimal getShippingCost() { return shippingCost; }
        public BigDecimal getTotal() { return total; }
        public String getAddressLine() { return addressLine; }
    }

    private static class SummaryMapper implements RowMapper<OrderSummary> {
        @Override public OrderSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderSummary s = new OrderSummary();
            s.orderId      = rs.getInt("order_id");
            s.orderDate    = rs.getObject("order_date", OffsetDateTime.class);
            s.statusTitle  = rs.getString("status_title");
            s.statusCode   = rs.getString("status_code");
            s.itemsCount   = rs.getInt("items_count");
            s.itemsSum     = rs.getBigDecimal("items_sum");
            s.shippingCost = rs.getBigDecimal("shipping_cost");
            s.total        = rs.getBigDecimal("computed_total_sum");
            s.addressLine  = rs.getString("address_line");
            return s;
        }
    }
}
