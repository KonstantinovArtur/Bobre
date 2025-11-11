// src/main/java/ru/forge/blacksmith_shop/api/controllers/UserApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.dto.UserRow;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.users.service.UserService;
import ru.forge.blacksmith_shop.users.service.UserInUseException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Users", description = "CRUD пользователей и поиск")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserApiController {

    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserApiController(UserRepository users, UserService userService, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // --- READ ALL (табличная проекция) ---
    @Operation(summary = "Список пользователей (проекция для таблицы)")
    @GetMapping
    public List<UserRow> list() {
        return users.findAllRows(); // login, email, roleName, createdAt
    }

    // --- READ ONE ---
    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public User getOne(@PathVariable Integer id) {
        return users.findById(id).orElseThrow(() -> new NoSuchElementException("Пользователь id=" + id + " не найден"));
    }

    // --- CREATE ---
    @Operation(summary = "Создать пользователя")
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserDto form) {
        // уникальность логина/почты (без учёта регистра)
        if (users.existsByLoginIgnoreCase(form.login())) {
            return ResponseEntity.status(409).body("Логин уже используется");
        }
        if (users.existsByEmailIgnoreCase(form.email())) {
            return ResponseEntity.status(409).body("Email уже используется");
        }

        User u = new User();
        u.setLogin(form.login().trim());
        u.setEmail(form.email().trim());
        u.setName(form.name() != null ? form.name().trim() : null);
        u.setPasswordHash(passwordEncoder.encode(form.rawPassword()));
        u.setCreatedAt(OffsetDateTime.now());

        // привязываем роль по id без лишнего запроса
        Role r = new Role();          // import ru.forge.blacksmith_shop.users.domain.Role;
        r.setRoleId(form.roleId());   // у Role должен быть setRoleId(Integer)
        u.setRole(r);
        User saved = users.saveAndFlush(u);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getUserId())).body(saved);
    }

    // --- UPDATE (профиль: имя, логин, email, роль) ---
    @Operation(summary = "Обновить профиль пользователя")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody UpdateUserDto form) {
        User u = users.findById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        // проверки уникальности среди «остальных»
        if (users.existsByLoginIgnoreCaseAndUserIdNot(form.login(), id)) {
            return ResponseEntity.status(409).body("Логин уже используется другим пользователем");
        }
        var emailClash = users.findByEmail(form.email());
        if (emailClash.isPresent() && !emailClash.get().getUserId().equals(id)) {
            return ResponseEntity.status(409).body("Email уже используется другим пользователем");
        }

        u.setLogin(form.login().trim());
        u.setEmail(form.email().trim());
        u.setName(form.name() != null ? form.name().trim() : null);
        if (form.roleId() != null) {
            Role r = new Role();
            r.setRoleId(form.roleId());
            u.setRole(r);
        }


        return ResponseEntity.ok(users.saveAndFlush(u));
    }

    // --- CHANGE PASSWORD ---
    @Operation(summary = "Сменить пароль пользователя")
    @PutMapping("/{id}/password")
    @Transactional
    public ResponseEntity<?> changePassword(@PathVariable Integer id, @Valid @RequestBody ChangePasswordDto form) {
        User u = users.findById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        u.setPasswordHash(passwordEncoder.encode(form.rawPassword()));
        users.saveAndFlush(u);
        return ResponseEntity.noContent().build();
    }

    // --- DELETE (с учётом заказов) ---
    @Operation(summary = "Удалить пользователя по ID")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            userService.deleteById(id); // кинет UserInUseException, если есть заказы
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserInUseException e) {
            return ResponseEntity.status(409).body("Нельзя удалить пользователя: есть связанные заказы");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("Удаление невозможно: есть связанные данные");
        }
    }

    // --- SEARCH (по части логина/почты) ---
    @Operation(summary = "Поиск пользователей по подстроке логина или email")
    @GetMapping("/search")
    public List<User> search(@RequestParam("q") String q) {
        String s = q.trim().toLowerCase();
        return users.findAll().stream()
                .filter(u -> u.getLogin().toLowerCase().contains(s) || u.getEmail().toLowerCase().contains(s))
                .limit(50)
                .toList();
    }

    // --- Локальные DTO ---
    public record CreateUserDto(
            @NotBlank @Size(min = 3, max = 100) String login,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 6, max = 100) String rawPassword,
            @NotNull @Positive Integer roleId,
            @Size(max = 150) String name
    ) {}

    public record UpdateUserDto(
            @NotBlank @Size(min = 3, max = 100) String login,
            @NotBlank @Email @Size(max = 255) String email,
            @Positive Integer roleId,
            @Size(max = 150) String name
    ) {}

    public record ChangePasswordDto(
            @NotBlank @Size(min = 6, max = 100) String rawPassword
    ) {}
}
