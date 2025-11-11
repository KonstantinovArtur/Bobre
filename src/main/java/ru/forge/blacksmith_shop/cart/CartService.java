// src/main/java/ru/forge/blacksmith_shop/cart/CartService.java
package ru.forge.blacksmith_shop.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

@Service
public class CartService {

    private final ProductRepository products;

    public CartService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public void addToCart(Cart cart, Integer productId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Количество должно быть > 0");

        Product p = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        // Пытаемся «зарезервировать» на складе
        int ok = products.tryReserveStock(productId, qty);
        if (ok == 0) {
            throw new IllegalArgumentException("Недостаточно товара на складе");
        }

        cart.add(new CartItem(p.getProductId(), p.getName(), p.getPriceFinal(), qty));
    }

    @Transactional
    public void removeFromCart(Cart cart, Integer productId) {
        CartItem removed = cart.remove(productId);
        if (removed != null) {
            products.releaseStock(productId, removed.getQty());
        }
    }

    @Transactional
    public void clearCart(Cart cart) {
        for (CartItem it : cart.getItems()) {
            products.releaseStock(it.getProductId(), it.getQty());
        }
        cart.clear();
    }
    @Transactional
    public void setQty(Cart cart, Integer productId, int newQty) {
        if (newQty < 0) throw new IllegalArgumentException("Количество не может быть отрицательным");

        // найдём текущую строку
        CartItem current = cart.getItems().stream()
                .filter(it -> productId.equals(it.getProductId()))
                .findFirst()
                .orElse(null);

        int oldQty = current != null ? current.getQty() : 0;
        if (oldQty == newQty) return;

        if (newQty == 0) {
            // удалить строку и освободить весь резерв
            removeFromCart(cart, productId);
            return;
        }

        if (oldQty < newQty) {
            // нужно увеличить — резервируем дельту
            int delta = newQty - oldQty;
            addToCart(cart, productId, delta); // внутри делает tryReserveStock(...)
        } else {
            // нужно уменьшить — освобождаем дельту, цену/имя не трогаем
            int delta = oldQty - newQty;
            if (current == null) throw new IllegalArgumentException("Строка корзины не найдена");
            current.setQty(newQty);
            products.releaseStock(productId, delta);
        }
    }
}
