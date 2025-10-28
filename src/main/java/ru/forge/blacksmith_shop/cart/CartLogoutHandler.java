// src/main/java/ru/forge/blacksmith_shop/cart/CartLogoutHandler.java
package ru.forge.blacksmith_shop.cart;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
@Component
public class CartLogoutHandler implements LogoutHandler {

    private final UserRepository users;
    private final CartStorageService storage;
    private final Cart sessionCart;

    public CartLogoutHandler(UserRepository users, CartStorageService storage, Cart sessionCart) {
        this.users = users;
        this.storage = storage;
        this.sessionCart = sessionCart;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        if (authentication != null) {
            users.findByLogin(authentication.getName())
                    .ifPresent(u -> storage.persistSessionCart(sessionCart, u.getUserId()));
        }
    }
}
