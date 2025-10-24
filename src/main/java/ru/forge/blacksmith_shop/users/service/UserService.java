package ru.forge.blacksmith_shop.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.forge.blacksmith_shop.users.dto.UserRow;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

import java.util.List;

@Service

public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) {
        this.users = users;
    }
    public List<UserRow> list() {
        return users.findAllRows();
    }
}
