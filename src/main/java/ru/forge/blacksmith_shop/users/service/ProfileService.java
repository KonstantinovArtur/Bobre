// src/main/java/ru/forge/blacksmith_shop/users/service/ProfileService.java
package ru.forge.blacksmith_shop.users.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.dto.PasswordChangeForm;
import ru.forge.blacksmith_shop.users.dto.ProfileForm;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Service
public class ProfileService {

    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;

    public ProfileService(UserRepository users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User getRequired(Integer userId) {
        return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Transactional
    public void updateProfile(Integer userId, ProfileForm form) {
        User u = getRequired(userId);

        String login = form.getLogin().trim();
        String name = form.getName() == null ? null : form.getName().trim();

        if (users.existsByLoginIgnoreCaseAndUserIdNot(login, userId)) {
            throw new IllegalArgumentException("Такой логин уже используется");
        }

        u.setLogin(login);
        u.setName(name);
        users.save(u);
    }

    @Transactional
    public void changePassword(Integer userId, PasswordChangeForm form) {
        User u = getRequired(userId);

        if (!encoder.matches(form.getCurrentPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Текущий пароль указан неверно");
        }
        if (!form.getNewPassword().equals(form.getConfirm())) {
            throw new IllegalArgumentException("Новый пароль и подтверждение не совпадают");
        }

        u.setPasswordHash(encoder.encode(form.getNewPassword()));
        users.save(u);
    }
}
