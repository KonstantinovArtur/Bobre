// AuthService.java
package ru.forge.blacksmith_shop.users.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.dto.RegisterForm;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserRepository users, RoleRepository roles, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public void register(RegisterForm form) {
        String login = form.getLogin().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (users.existsByLogin(login)) {
            throw new IllegalArgumentException("Логин уже занят");
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email уже используется");
        }
        // совпадение паролей уже проверяет bean-validation, но подстрахуемся:
        if (!form.getPassword().equals(form.getConfirm())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        Role role = roles.findByRoleName("User")
                .orElseThrow(() -> new IllegalStateException("Роль 'User' не найдена в БД"));

        User u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setName(form.getName() == null ? null : form.getName().trim());
        u.setPasswordHash(encoder.encode(form.getPassword()));
        u.setRole(role);

        try {
            users.save(u);
        } catch (DataIntegrityViolationException ex) {
            // если вдруг впёрлись в уникальный индекс
            throw new IllegalArgumentException("Пользователь с таким логином/почтой уже существует");
        }
    }
}
