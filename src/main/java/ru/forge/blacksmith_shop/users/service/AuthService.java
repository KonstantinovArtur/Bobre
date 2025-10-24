// ru.forge.blacksmith_shop.users.service.AuthService
package ru.forge.blacksmith_shop.users.service;

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
        users.findByLogin(form.getLogin()).ifPresent(u -> { throw new IllegalArgumentException("Логин уже занят"); });
        users.findByEmail(form.getEmail()).ifPresent(u -> { throw new IllegalArgumentException("Email уже используется"); });
        if (!form.getPassword().equals(form.getConfirm())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        Role role = roles.findByRoleName("User")
                .orElseThrow(() -> new IllegalStateException("Роль 'User' не найдена в БД"));

        User u = new User();
        u.setLogin(form.getLogin());
        u.setEmail(form.getEmail());
        u.setName(form.getName());
        u.setPasswordHash(encoder.encode(form.getPassword())); // <-- BCrypt
        u.setRole(role);

        users.save(u);
    }
}
