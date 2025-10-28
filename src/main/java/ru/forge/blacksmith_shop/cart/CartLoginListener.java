// src/main/java/ru/forge/blacksmith_shop/cart/CartLoginListener.java
package ru.forge.blacksmith_shop.cart;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Component
public class CartLoginListener {

    private final UserRepository users;
    private final CartStorageService storage;
    private final Cart sessionCart;

    // ЯВНЫЙ конструктор для final-полей
    public CartLoginListener(UserRepository users,
                             CartStorageService storage,
                             Cart sessionCart) {
        this.users = users;
        this.storage = storage;
        this.sessionCart = sessionCart;
    }

    @EventListener
    public void onLogin(InteractiveAuthenticationSuccessEvent ev) {
        var auth = ev.getAuthentication();
        if (auth == null) return;

        users.findByLogin(auth.getName())
                .ifPresent(u -> storage.mergeSessionIntoUserAndReload(sessionCart, u.getUserId()));
    }
}
