package ru.forge.blacksmith_shop.users.web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    // ✅ обычный конструктор (вместо Lombok)
    public AdminUserController(UserRepository users,
                               RoleRepository roles,
                               PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.findAll());
        model.addAttribute("roles", roles.findAll());
        return "admin/users";
    }

    /** Создание пользователя (минимальный набор полей). */
    @PostMapping("/create")
    public String create(@RequestParam String login,
                         @RequestParam String email,
                         @RequestParam String name,
                         @RequestParam String password,
                         @RequestParam Integer roleId,
                         RedirectAttributes ra) {
        Role role = roles.findById(roleId).orElseThrow();
        User u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setName(name);
        u.setRole(role);
        u.setPasswordHash(passwordEncoder.encode(password));
        users.save(u);
        ra.addFlashAttribute("ok", "Пользователь создан");
        return "redirect:/admin/users";
    }

    /** Смена роли пользователю. */
    @PostMapping("/{userId}/role")
    public String changeRole(@PathVariable Integer userId,
                             @RequestParam Integer roleId,
                             RedirectAttributes ra) {
        var u = users.findById(userId).orElseThrow();
        var r = roles.findById(roleId).orElseThrow();
        u.setRole(r);
        users.save(u);
        ra.addFlashAttribute("ok", "Роль обновлена");
        return "redirect:/admin/users";
    }

    /** Удаление пользователя. */
    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Integer userId, RedirectAttributes ra) {
        users.deleteById(userId);
        ra.addFlashAttribute("ok", "Пользователь удалён");
        return "redirect:/admin/users";
    }
}
