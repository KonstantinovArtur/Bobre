// src/main/java/ru/forge/blacksmith_shop/fav/FavoritesStorageService.java
package ru.forge.blacksmith_shop.fav;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoritesStorageService {

    private final FavoritesRepository favoritesRepo;

    public FavoritesStorageService(FavoritesRepository favoritesRepo) {
        this.favoritesRepo = favoritesRepo;
    }

    /** Смерджить сессию в БД и перезалить избранное из БД в сессию. */
    @Transactional
    public void mergeSessionIntoUserAndReload(Favorites sessionFav, Integer userId) {
        // session -> DB
        for (Integer pid : sessionFav.getItems()) {
            favoritesRepo.addIfAbsent(userId, pid);
        }
        // DB -> session
        sessionFav.clear();
        List<Integer> ids = favoritesRepo.findByUserId(userId);
        for (Integer pid : ids) {
            sessionFav.add(pid);
        }
    }

    /** Сохранить текущую сессию в БД (например, на логаут). */
    @Transactional
    public void persistSessionFavorites(Favorites sessionFav, Integer userId) {
        favoritesRepo.clearByUser(userId);
        for (Integer pid : sessionFav.getItems()) {
            favoritesRepo.addIfAbsent(userId, pid);
        }
    }
}
