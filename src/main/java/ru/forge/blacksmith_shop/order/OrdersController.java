package ru.forge.blacksmith_shop.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

import java.util.List;

@Controller
public class OrdersController {

    private final OrderDao orders;
    private final UserRepository users;

    public OrdersController(OrderDao orders, UserRepository users) {
        this.orders = orders;
        this.users = users;
    }

    @GetMapping("/orders")
    public String list(org.springframework.security.core.Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/login";
        }
        var u = users.findByLogin(auth.getName()).orElse(null);
        if (u == null) {
            model.addAttribute("orders", List.of());
            return "orders/list";
        }
        model.addAttribute("orders", orders.findSummariesByUser(u.getUserId()));
        return "orders/list";
    }
}
