// src/main/java/ru/forge/blacksmith_shop/order/repo/OrderAddressRepository.java
package ru.forge.blacksmith_shop.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.forge.blacksmith_shop.order.domain.OrderAddress;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Integer> {
    // при желании:
    // List<OrderAddress> findAllByOrderByAddressIdDesc();
}
