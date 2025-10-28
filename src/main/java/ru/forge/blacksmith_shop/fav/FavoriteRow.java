// src/main/java/ru/forge/blacksmith_shop/fav/FavoriteRow.java
package ru.forge.blacksmith_shop.fav;

import jakarta.persistence.*;

@Entity
@Table(name = "user_favorites")
public class FavoriteRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")       // можно завести surrogate key, если хочешь
    private Long id;           // либо вообще не хранить сущность и не использовать JpaRepository

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    public FavoriteRow() {}
    public FavoriteRow(Integer userId, Integer productId) {
        this.userId = userId;
        this.productId = productId;
    }
    // getters/setters ...
}
