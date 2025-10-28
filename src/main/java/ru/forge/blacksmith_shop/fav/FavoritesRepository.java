// src/main/java/ru/forge/blacksmith_shop/fav/FavoritesRepository.java
package ru.forge.blacksmith_shop.fav;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritesRepository extends JpaRepository<FavoriteRow, Long> {

    // Храним “сырой” вид строки (можно без сущности, но так удобнее)
    @Query(value = "SELECT product_id FROM user_favorites WHERE user_id = :userId", nativeQuery = true)
    List<Integer> findProductIdsByUserId(Integer userId);

    @Modifying
    @Query(value = """
            INSERT INTO user_favorites(user_id, product_id)
            VALUES (:userId, :productId)
            ON CONFLICT (user_id, product_id) DO NOTHING
            """, nativeQuery = true)
    int add(Integer userId, Integer productId);

    @Modifying
    @Query(value = "DELETE FROM user_favorites WHERE user_id = :userId AND product_id = :productId", nativeQuery = true)
    int remove(Integer userId, Integer productId);

    @Modifying
    @Query(value = "DELETE FROM user_favorites WHERE user_id = :userId", nativeQuery = true)
    int clearByUser(Integer userId);
}
