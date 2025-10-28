// src/main/java/ru/forge/blacksmith_shop/cart/CartController.java
package ru.forge.blacksmith_shop.cart;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final Cart cart;                  // session-scoped корзина
    private final ProductRepository products; // оставлено, если где-то понадобится
    private final UserRepository users;
    private final CartStorageService storage;
    private final CartService cartService;    // резерв/освобождение склада

    // ЯВНЫЙ конструктор (вместо @RequiredArgsConstructor)
    public CartController(Cart cart,
                          ProductRepository products,
                          UserRepository users,
                          CartStorageService storage,
                          CartService cartService) {
        this.cart = cart;
        this.products = products;
        this.users = users;
        this.storage = storage;
        this.cartService = cartService;
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Integer productId,
                      @RequestParam(defaultValue = "1") int qty,
                      Authentication auth,
                      RedirectAttributes ra) {
        cartService.addToCart(cart, productId, qty); // резерв на складе + в сессию

        // Если пользователь вошёл — сразу сохраняем сессию в БД
        if (auth != null && auth.isAuthenticated()) {
            users.findByLogin(auth.getName())
                    .ifPresent(u -> storage.persistSessionCart(cart, u.getUserId()));
        }
        ra.addFlashAttribute("ok", "Добавлено в корзину");
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Integer productId,
                         Authentication auth,
                         RedirectAttributes ra) {
        cartService.removeFromCart(cart, productId);

        if (auth != null && auth.isAuthenticated()) {
            users.findByLogin(auth.getName())
                    .ifPresent(u -> storage.persistSessionCart(cart, u.getUserId()));
        }
        ra.addFlashAttribute("ok", "Удалено из корзины");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(Authentication auth, RedirectAttributes ra) {
        cartService.clearCart(cart);

        if (auth != null && auth.isAuthenticated()) {
            users.findByLogin(auth.getName())
                    .ifPresent(u -> storage.persistSessionCart(cart, u.getUserId()));
        }
        ra.addFlashAttribute("ok", "Корзина очищена");
        return "redirect:/cart";
    }
}
