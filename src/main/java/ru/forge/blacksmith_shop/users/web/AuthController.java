// AuthController.java
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
                             BindingResult br) {
        if (br.hasErrors()) {
            return "auth/register";
        }
        try {
            auth.register(form);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "Ошибка регистрации" : ex.getMessage();
            // Маппим популярные тексты на конкретные поля (чтобы показать рядом с инпутом).
            if (msg.contains("Логин")) {
                br.rejectValue("login", "login.unique", msg);
            } else if (msg.contains("Email") || msg.contains("почт")) {
                br.rejectValue("email", "email.unique", msg);
            } else if (msg.contains("Пароли")) {
                br.rejectValue("confirm", "passwords.match", msg);
            } else {
                br.reject("registration.error", msg); // глобальная ошибка
            }
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}
