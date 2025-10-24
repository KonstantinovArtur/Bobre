package ru.forge.blacksmith_shop.users.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.forge.blacksmith_shop.users.service.UserService;

@Controller

public class AdminUsersController {

    private final UserService userService;
    public AdminUsersController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.list());
        return "admin/users";
    }
}
