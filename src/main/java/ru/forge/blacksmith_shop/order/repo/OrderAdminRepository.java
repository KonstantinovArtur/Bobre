package ru.forge.blacksmith_shop.order.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.forge.blacksmith_shop.order.web.OrderAdminRow;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderAdminRepository {

    private final JdbcTemplate jdbc;

    // явный конструктор
    public OrderAdminRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int confirmIfCreated(int orderId) {
        String sql = """
        UPDATE orders
        SET status_id = (
            SELECT status_id FROM order_statuses WHERE code = 'in_transit'
        )
        WHERE order_id = ?
          AND status_id = (
              SELECT status_id FROM order_statuses WHERE code = 'created'
          )
        """;

        return jdbc.update(sql, orderId);
    }


    public List<OrderAdminRow> findAll() {
        String sql = """
            SELECT
                order_id                                        AS orderId,
                ('ORD-' || lpad(order_id::text, 6, '0'))        AS idOrder,
                order_date                                      AS orderDate,
                user_id                                         AS userId,
                user_login                                      AS userLogin,
                total                                           AS total,
                address_line                                    AS addressLine,
                shipping_cost                                   AS shippingCost,
                UPPER(status_code)                              AS statusCode,
                status_title                                    AS statusTitle,
                items_count                                     AS itemsCount
            FROM vw_orders_manager
            ORDER BY order_date DESC, order_id DESC
            """;

        return jdbc.query(sql, (rs, i) -> {
            Timestamp ts = rs.getTimestamp("orderDate");
            LocalDateTime od = (ts != null ? ts.toLocalDateTime() : null);
            Integer itemsCount = rs.getObject("itemsCount") == null ? null : rs.getInt("itemsCount");

            return new OrderAdminRow(
                    rs.getInt("orderId"),
                    rs.getString("idOrder"),
                    od,
                    rs.getInt("userId"),
                    rs.getString("userLogin"),
                    rs.getBigDecimal("total"),
                    rs.getString("addressLine"),
                    rs.getBigDecimal("shippingCost"),
                    rs.getString("statusCode"),
                    rs.getString("statusTitle"),
                    itemsCount
            );
        });

    }

    public int markInTransitIfCreated(int orderId) {
        final String sql = """
            UPDATE orders o
            SET status_id = s2.status_id
            FROM order_statuses s1,
                 order_statuses s2
            WHERE o.order_id = ?
              AND s1.status_id = o.status_id
              AND s1.code = 'created'
              AND s2.code = 'in_transit'
            """;
        return jdbc.update(sql, orderId);
    }

    // при желании можно добавить завершение:
    public int markReceivedIfInTransit(int orderId) {
        final String sql = """
            UPDATE orders o
            SET status_id = s2.status_id
            FROM order_statuses s1,
                 order_statuses s2
            WHERE o.order_id = ?
              AND s1.status_id = o.status_id
              AND s1.code = 'in_transit'
              AND s2.code = 'received'
            """;
        return jdbc.update(sql, orderId);
    }
    public long countByUserId(Integer userId) {
        Long n = jdbc.queryForObject(
                "select count(*) from orders where user_id = ?",
                Long.class, userId
        );
        return n != null ? n : 0L;
    }
}
