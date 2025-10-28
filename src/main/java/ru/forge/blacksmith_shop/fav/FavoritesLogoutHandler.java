// ru.forge.blacksmith_shop.fav.FavoritesLogoutHandler
package ru.forge.blacksmith_shop.fav;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Component
public class FavoritesLogoutHandler implements LogoutSuccessHandler {

    private final UserRepository users;
    private final FavoritesStorageService storage;
    private final Favorites favorites; // сессионный proxy

    public FavoritesLogoutHandler(UserRepository users,
                                  FavoritesStorageService storage,
                                  Favorites favorites) {
        this.users = users;
        this.storage = storage;
        this.favorites = favorites;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws java.io.IOException {
        if (authentication != null) {
            users.findByLogin(authentication.getName())
                    .ifPresent(u -> storage.persistSessionFavorites(favorites, u.getUserId()));
        }
        // НЕ делаем redirect тут, если у вас уже общий хендлер делает redirect
    }
}
