// src/main/java/ru/forge/blacksmith_shop/users/service/RoleService.java
package ru.forge.blacksmith_shop.users.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Service
public class RoleService {

    private final RoleRepository roles;
    private final UserRepository users;

    public RoleService(RoleRepository roles, UserRepository users) {
        this.roles = roles;
        this.users = users;
    }

    @Transactional
    public void deleteById(Integer roleId) {
        if (!roles.existsById(roleId)) {
            throw new EntityNotFoundException("Роль не найдена: id=" + roleId);
        }
        long cnt = users.countByRole_RoleId(roleId); // или countByRoleId(roleId)
        if (cnt > 0) {
            throw new RoleInUseException(cnt);
        }
        roles.deleteById(roleId);
    }
}
