package ru.forge.blacksmith_shop.users.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.dto.AdminCreateUserForm;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.users.service.UserInUseException;
import ru.forge.blacksmith_shop.users.service.UserService;

import java.time.OffsetDateTime;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    // ✅ обычный конструктор (вместо Lombok)
    public AdminUserController(UserRepository users,
                               RoleRepository roles,
                               PasswordEncoder passwordEncoder, UserService userService) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }
    // 1) ДЕФОЛТНЫЙ БИНДИНГ-ОБЪЕКТ — ВСЕГДА ЕСТЬ В МОДЕЛИ
    @ModelAttribute("createForm")
    public AdminCreateUserForm createForm() {
        return new AdminCreateUserForm();
    }

    // 2) ЕДИНСТВЕННЫЙ GET: кладём списки
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.findAll());
        model.addAttribute("roles", roles.findAll());
        return "admin/users";
    }

    // 3) POST /create с валидацией + флешами (как раньше делали)
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("createForm") AdminCreateUserForm form,
                         BindingResult br,
                         RedirectAttributes ra) {

        // 1) нормализация
        String login = form.getLogin() == null ? "" : form.getLogin().trim();
        String email = form.getEmail() == null ? "" : form.getEmail().trim().toLowerCase();
        String name  = form.getName()  == null ? null : form.getName().trim();

        form.setLogin(login);
        form.setEmail(email);
        form.setName(name);

        // 2) валидация дубликатов (CI) — только если нет других ошибок по этим полям
        if (!br.hasFieldErrors("login") && users.existsLoginCi(login)) {
            br.rejectValue("login", "login.duplicate", "Логин уже занят");
        }
        if (!br.hasFieldErrors("email") && users.existsEmailCi(email)) {
            br.rejectValue("email", "email.duplicate", "Email уже используется");
        }

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.createForm", br);
            ra.addFlashAttribute("createForm", form);
            return "redirect:/admin/users";
        }

        var role = roles.findById(form.getRoleId()).orElseThrow();

        var u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setName(name);
        u.setRole(role);
        u.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        u.setCreatedAt(java.time.OffsetDateTime.now());

        try {
            users.saveAndFlush(u);
            ra.addFlashAttribute("ok", "Пользователь создан");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // 3) аккуратно разбираем причину
            String msg = "Не удалось создать пользователя";
            Throwable root = ex.getMostSpecificCause();
            String cause = (root != null ? root.getMessage() : "");
            // Если в БД есть уникальные индексы — различим их по имени/тексте
            if (cause != null) {
                String c = cause.toLowerCase();
                if (c.contains("unique") && c.contains("login")) {
                    br.rejectValue("login", "login.duplicate", "Логин уже занят");
                } else if (c.contains("unique") && c.contains("email")) {
                    br.rejectValue("email", "email.duplicate", "Email уже используется");
                } else {
                    br.reject("create.failed", msg);
                }
            } else {
                br.reject("create.failed", msg);
            }
            ra.addFlashAttribute("org.springframework.validation.BindingResult.createForm", br);
            ra.addFlashAttribute("createForm", form);
        }
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
        try {
            userService.deleteById(userId);
            ra.addFlashAttribute("ok", "Пользователь удалён.");
        } catch (UserInUseException e) {
            ra.addFlashAttribute("error",
                    e.getMessage() + ". Сначала обработайте/перенесите его заказы.");
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", "Пользователь не найден.");
        }
        return "redirect:/admin/users";
    }
}
