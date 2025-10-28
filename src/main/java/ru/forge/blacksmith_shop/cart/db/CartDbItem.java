// src/main/java/ru/forge/blacksmith_shop/cart/db/CartDbItem.java
package ru.forge.blacksmith_shop.cart.db;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name="uq_cart_user_product",
                columnNames = {"user_id","product_id"}))
public class CartDbItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Integer getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }

    public void setUserId(Integer userId) { this.userId = userId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
