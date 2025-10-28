// src/main/java/ru/forge/blacksmith_shop/cart/CartController.java
package ru.forge.blacksmith_shop.cart;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.order.OrderService;
import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final Cart cart;                  // session bean
    private final ProductRepository products;
    private final UserRepository users;
    private final CartStorageService storage;
    private final CartService cartService;    // резерв/освобождение склада
    private final OrderService orderService;  // <-- добавили

    // Явный конструктор без Lombok
    public CartController(Cart cart,
                          ProductRepository products,
                          UserRepository users,
                          CartStorageService storage,
                          CartService cartService,
                          OrderService orderService) {
        this.cart = cart;
        this.products = products;
        this.users = users;
        this.storage = storage;
        this.cartService = cartService;
        this.orderService = orderService; // <-- важно
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
        cartService.addToCart(cart, productId, qty);

        if (auth != null) {
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

        if (auth != null) {
            users.findByLogin(auth.getName())
                    .ifPresent(u -> storage.persistSessionCart(cart, u.getUserId()));
        }
        ra.addFlashAttribute("ok", "Удалено из корзины");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(Authentication auth, RedirectAttributes ra) {
        cartService.clearCart(cart);

        if (auth != null) {
            users.findByLogin(auth.getName())
                    .ifPresent(u -> storage.persistSessionCart(cart, u.getUserId()));
        }
        ra.addFlashAttribute("ok", "Корзина очищена");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(name = "address", required = false) String address,
                           @RequestParam(name = "shipping", required = false) BigDecimal shipping,
                           Authentication auth,
                           RedirectAttributes ra) {

        if (auth == null) {
            ra.addFlashAttribute("error", "Авторизуйтесь, чтобы оформить заказ");
            return "redirect:/login";
        }

        var userOpt = users.findByLogin(auth.getName());
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/cart";
        }

        var created = orderService.checkout(
                cart,
                userOpt.get().getUserId(),
                (address == null || address.isBlank()) ? "Самовывоз" : address,
                (shipping == null) ? BigDecimal.ZERO : shipping
        );

        ra.addFlashAttribute("ok",
                "Заказ №" + created.getOrderId() + " оформлен на сумму " + created.getTotal() + " ₽");
        return "redirect:/orders";
    }
}
