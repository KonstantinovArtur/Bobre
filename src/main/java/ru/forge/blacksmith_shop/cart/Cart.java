// src/main/java/ru/forge/blacksmith_shop/cart/Cart.java
package ru.forge.blacksmith_shop.cart;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.*;

@Component
@SessionScope
public class Cart {
    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public void add(CartItem it) {
        items.merge(it.getProductId(), it, (oldIt, newIt) -> {
            oldIt.setQty(oldIt.getQty() + newIt.getQty());
            return oldIt;
        });
    }

    public CartItem remove(Integer productId) {
        return items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public BigDecimal getTotal() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
