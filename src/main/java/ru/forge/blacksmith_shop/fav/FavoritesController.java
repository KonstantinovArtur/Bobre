// src/main/java/ru/forge/blacksmith_shop/fav/FavoritesController.java
package ru.forge.blacksmith_shop.fav;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    private final Favorites favorites;                    // session bean
    private final ProductRepository products;
    private final UserRepository users;
    private final FavoritesStorageService storage;

    public FavoritesController(Favorites favorites,
                               ProductRepository products,
                               UserRepository users,
                               FavoritesStorageService storage) {
        this.favorites = favorites;
        this.products = products;
        this.users = users;
        this.storage = storage;
    }

    @GetMapping
    public String view(Model model) {
        Set<Integer> ids = favorites.getItems();
        List<Product> items = ids.isEmpty()
                ? Collections.emptyList()
                : products.findAllById(ids);
        model.addAttribute("items", items);
        model.addAttribute("ids", ids); // удобно для подсветки “в избранном” в шаблонах
        return "favorites/list";        // при необходимости поменяйте на ваш путь к шаблону
    }

    @PostMapping("/add")
    public String add(@RequestParam Integer productId,
                      Authentication auth,
                      RedirectAttributes ra) {
        favorites.add(productId);
        persistIfAuthenticated(auth);
        ra.addFlashAttribute("ok", "Добавлено в избранное");
        return "redirect:/favorites";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Integer productId,
                         Authentication auth,
                         RedirectAttributes ra) {
        favorites.remove(productId);
        persistIfAuthenticated(auth);
        ra.addFlashAttribute("ok", "Удалено из избранного");
        return "redirect:/favorites";
    }

    /** Удобный эндпоинт для кнопки “сердечко”: переключение состояния. */
    @PostMapping("/toggle")
    public String toggle(@RequestParam Integer productId,
                         Authentication auth,
                         RedirectAttributes ra) {
        if (favorites.contains(productId)) {
            favorites.remove(productId);
            ra.addFlashAttribute("ok", "Удалено из избранного");
        } else {
            favorites.add(productId);
            ra.addFlashAttribute("ok", "Добавлено в избранное");
        }
        persistIfAuthenticated(auth);
        return "redirect:/favorites";
    }

    @PostMapping("/clear")
    public String clear(Authentication auth, RedirectAttributes ra) {
        favorites.clear();
        persistIfAuthenticated(auth);
        ra.addFlashAttribute("ok", "Избранное очищено");
        return "redirect:/favorites";
    }

    /** Если пользователь залогинен — сохраняем текущее избранное из сессии в БД. */
    private void persistIfAuthenticated(Authentication auth) {
        if (auth != null) {
            users.findByLogin(auth.getName())
                    .ifPresent(u -> storage.persistSessionFavorites(favorites, u.getUserId()));
        }
    }
}
