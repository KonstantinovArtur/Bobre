package ru.forge.blacksmith_shop.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.cart.db.CartDbItem;
import ru.forge.blacksmith_shop.cart.db.CartRepository;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

import java.util.List;

@Service
public class CartStorageService {

    private final CartRepository carts;
    private final ProductRepository products;

    // ЯВНЫЙ КОНСТРУКТОР — решает ошибку
    public CartStorageService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    /** Смержить сессионную корзину в БД и перезалить в сессию содержимое из БД. */
    @Transactional
    public void mergeSessionIntoUserAndReload(Cart sessionCart, Integer userId) {
        // 1) сессия → БД (upsert)
        for (CartItem si : sessionCart.getItems()) {
            carts.addOrIncrement(userId, si.getProductId(), si.getQty());
        }

        // 2) БД → сессия (пересобрать с актуальными именами/ценой)
        sessionCart.clear();
        List<CartDbItem> db = carts.findByUserId(userId);
        for (CartDbItem li : db) {
            Product p = products.findById(li.getProductId()).orElse(null);
            if (p == null) continue; // вдруг удалили товар

            sessionCart.add(new CartItem(
                    p.getProductId(),
                    p.getName(),
                    p.getPriceFinal(), // цена "на сейчас"
                    li.getQuantity()
            ));
        }
    }

    /** Сохранить текущую сессию в БД (например, на логаут). */
    @Transactional
    public void persistSessionCart(Cart sessionCart, Integer userId) {
        carts.clearByUser(userId);
        for (CartItem si : sessionCart.getItems()) {
            carts.addOrIncrement(userId, si.getProductId(), si.getQty());
        }
    }
}
