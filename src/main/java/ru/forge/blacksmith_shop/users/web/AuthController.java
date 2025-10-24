package ru.forge.blacksmith_shop.users.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.users.dto.RegisterForm;
import ru.forge.blacksmith_shop.users.service.AuthService;

@Controller
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("form") @Valid RegisterForm form,
                             BindingResult br, Model model) {
        if (br.hasErrors()) {
            return "auth/register";
        }
        try {
            auth.register(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
        // после регистрации можно логинить автоматически, но пока — просто на /login
        return "redirect:/login?registered";
    }
}
