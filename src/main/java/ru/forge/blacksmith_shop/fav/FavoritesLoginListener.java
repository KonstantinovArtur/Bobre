// ru.forge.blacksmith_shop.fav.FavoritesLoginListener
package ru.forge.blacksmith_shop.fav;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Component
public class FavoritesLoginListener {

    private final UserRepository users;
    private final FavoritesStorageService storage;
    private final Favorites favorites;

    public FavoritesLoginListener(UserRepository users,
                                  FavoritesStorageService storage,
                                  Favorites favorites) {
        this.users = users;
        this.storage = storage;
        this.favorites = favorites;
    }

    @EventListener
    public void onLogin(InteractiveAuthenticationSuccessEvent ev) {
        users.findByLogin(ev.getAuthentication().getName())
                .ifPresent(u -> storage.mergeSessionIntoUserAndReload(favorites, u.getUserId()));
    }
}
