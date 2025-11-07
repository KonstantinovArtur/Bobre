// src/main/java/ru/forge/blacksmith_shop/order/AddressRepository.java
package ru.forge.blacksmith_shop.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AddressRepository {
    private final JdbcTemplate jdbc;

    public AddressRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Все уникальные адреса из order_addresses, отсортированы по "самому свежему" использованию. */
    public List<String> findAllDistinct() {
        String sql = """
            SELECT t.address_line
            FROM (
                SELECT oa.address_line, MAX(oa.address_id) AS last_id
                FROM order_addresses oa
                GROUP BY oa.address_line
            ) t
            ORDER BY t.last_id DESC
            """;
        return jdbc.queryForList(sql, String.class);
    }

}
