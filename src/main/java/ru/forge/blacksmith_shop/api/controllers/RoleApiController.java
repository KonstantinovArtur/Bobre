// src/main/java/ru/forge/blacksmith_shop/api/controllers/RoleApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Roles", description = "CRUD ролей пользователей")
@RestController
@RequestMapping("/api/roles")
@Validated
public class RoleApiController {

    private final RoleRepository roles;

    public RoleApiController(RoleRepository roles) {
        this.roles = roles;
    }

    // ===== READ ALL =====
    @Operation(summary = "Список всех ролей")
    @GetMapping
    public List<Role> list() {
        return roles.findAll();
    }

    // ===== READ ONE =====
    @Operation(summary = "Получить роль по ID")
    @GetMapping("/{id}")
    public Role getOne(@PathVariable Integer id) {
        return roles.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Роль id=" + id + " не найдена"));
    }

    // ===== CREATE =====
    @Operation(summary = "Создать роль")
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@Valid @RequestBody RoleDto form) {
        String name = form.roleName().trim();
        if (roles.existsByRoleNameIgnoreCase(name)) {
            return ResponseEntity.status(409).body("Роль \"" + name + "\" уже существует");
        }
        Role r = new Role();
        r.setRoleName(name);
        Role saved = roles.saveAndFlush(r);
        return ResponseEntity.created(URI.create("/api/roles/" + saved.getRoleId())).body(saved);
    }

    // ===== UPDATE =====
    @Operation(summary = "Переименовать роль")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody RoleDto form) {
        Role r = roles.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Роль id=" + id + " не найдена"));
        String name = form.roleName().trim();
        if (roles.existsByRoleNameIgnoreCaseAndRoleIdNot(name, id)) {
            return ResponseEntity.status(409).body("Имя роли уже занято другой ролью");
        }
        r.setRoleName(name);
        return ResponseEntity.ok(roles.saveAndFlush(r));
    }

    // ===== DELETE =====
    @Operation(summary = "Удалить роль по ID")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Role r = roles.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Роль id=" + id + " не найдена"));
        try {
            roles.delete(r);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // на роль, вероятно, ссылаются пользователи (FK)
            return ResponseEntity.status(409).body("Нельзя удалить: на роль ссылаются пользователи");
        }
    }

    // ===== SEARCH =====
    @Operation(summary = "Поиск ролей по подстроке названия")
    @GetMapping("/search")
    public List<Role> search(@RequestParam("q") String q) {
        String s = q.trim().toLowerCase();
        return roles.findAll().stream()
                .filter(r -> r.getRoleName() != null && r.getRoleName().toLowerCase().contains(s))
                .limit(50)
                .toList();
    }

    // локальный DTO с такой же валидацией, как у сущности Role
    public record RoleDto(
            @NotBlank(message = "Название роли не может быть пустым")
            @Size(min = 3, max = 20, message = "Название роли должно быть от 3 до 20 символов")
            @Pattern(regexp = "^[A-Za-zА-Яа-я][A-Za-zА-Яа-я0-9_-]*$",
                    message = "Роль должна начинаться с буквы и содержать только буквы, цифры, дефис или подчёркивание")
            String roleName
    ) {}
}
