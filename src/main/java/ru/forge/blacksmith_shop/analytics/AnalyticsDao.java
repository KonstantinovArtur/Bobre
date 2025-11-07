package ru.forge.blacksmith_shop.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.forge.blacksmith_shop.analytics.dto.CategoryRevenueDto;
import ru.forge.blacksmith_shop.analytics.dto.MonthlySalesDto;
import ru.forge.blacksmith_shop.analytics.dto.TopProductDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AnalyticsDao {
    private final JdbcTemplate jdbc;

    public AnalyticsDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Продажи по месяцам за период (по orders.total_sum)
    public List<MonthlySalesDto> getMonthlySales(LocalDate from, LocalDate to) {
        String sql = """
                    SELECT to_char(o.order_date, 'YYYY-MM') AS month,
                           SUM(o.total_sum)                 AS total
                    FROM orders o
                    WHERE (:fromDate IS NULL OR o.order_date >= :fromDate::timestamp)
                      AND (:toDate   IS NULL OR o.order_date <  (:toDate::timestamp + interval '1 day'))
                    GROUP BY 1
                    ORDER BY 1
                """.replace(":fromDate", from == null ? "NULL" : "'" + from + "'")
                .replace(":toDate", to == null ? "NULL" : "'" + to + "'");

        return jdbc.query(sql, (rs, __) -> mapMonthly(rs));
    }

    private MonthlySalesDto mapMonthly(ResultSet rs) throws SQLException {
        return new MonthlySalesDto(
                rs.getString("month"),
                rs.getBigDecimal("total")
        );
    }

    // Топ-товары по количеству и выручке за период
    public List<TopProductDto> getTopProducts(LocalDate from, LocalDate to, int limit) {
        String sql = """
                    SELECT p.name AS item_name,
                           SUM(oi.quantity) AS total_sold,
                           SUM(oi.quantity * oi.price_per_unit) AS revenue
                    FROM order_items oi
                    JOIN orders o   ON o.order_id = oi.order_id
                    JOIN products p ON p.product_id = oi.product_id
                    WHERE (:fromDate IS NULL OR o.order_date >= :fromDate::timestamp)
                      AND (:toDate   IS NULL OR o.order_date <  (:toDate::timestamp + interval '1 day'))
                    GROUP BY p.name
                    ORDER BY total_sold DESC
                    LIMIT %d
                """.formatted(Math.max(1, limit))
                .replace(":fromDate", from == null ? "NULL" : "'" + from + "'")
                .replace(":toDate", to == null ? "NULL" : "'" + to + "'");

        return jdbc.query(sql, (rs, __) -> mapTop(rs));
    }

    private TopProductDto mapTop(ResultSet rs) throws SQLException {
        return new TopProductDto(
                rs.getString("item_name"),
                rs.getLong("total_sold"),
                rs.getBigDecimal("revenue")
        );
    }

    public List<CategoryRevenueDto> getRevenueByCategory(LocalDate from, LocalDate to) {
        String sql = """
                    SELECT c.name AS category,
                           SUM(oi.quantity * oi.price_per_unit) AS revenue
                    FROM order_items oi
                    JOIN orders o   ON o.order_id = oi.order_id
                    JOIN products p ON p.product_id = oi.product_id
                    JOIN categories c ON c.category_id = p.category_id
                    WHERE (:fromDate IS NULL OR o.order_date >= :fromDate::timestamp)
                      AND (:toDate   IS NULL OR o.order_date <  (:toDate::timestamp + interval '1 day'))
                    GROUP BY c.name
                    ORDER BY revenue DESC
                """.replace(":fromDate", from == null ? "NULL" : "'" + from + "'")
                .replace(":toDate", to == null ? "NULL" : "'" + to + "'");

        return jdbc.query(sql, (rs, __) ->
                new CategoryRevenueDto(rs.getString("category"), rs.getBigDecimal("revenue"))
        );
    }
}
