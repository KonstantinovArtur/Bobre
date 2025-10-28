// src/main/java/ru/forge/blacksmith_shop/cart/db/CartRepository.java
package ru.forge.blacksmith_shop.cart.db;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartDbItem, Integer> {

    List<CartDbItem> findByUserId(Integer userId);

    Optional<CartDbItem> findByUserIdAndProductId(Integer userId, Integer productId);

    @Modifying
    @Query(value = """
        insert into cart_items(user_id, product_id, quantity)
        values (:uid, :pid, :qty)
        on conflict (user_id, product_id)
        do update set quantity = cart_items.quantity + excluded.quantity
        """, nativeQuery = true)
    int addOrIncrement(@Param("uid") Integer userId,
                       @Param("pid") Integer productId,
                       @Param("qty") int qty);

    @Modifying
    @Query(value = "delete from cart_items where user_id=:uid and product_id=:pid", nativeQuery = true)
    int deleteLine(@Param("uid") Integer userId, @Param("pid") Integer productId);

    @Modifying
    @Query(value = "delete from cart_items where user_id=:uid", nativeQuery = true)
    int clearByUser(@Param("uid") Integer userId);
}
