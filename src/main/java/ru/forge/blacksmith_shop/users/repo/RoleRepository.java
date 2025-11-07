package ru.forge.blacksmith_shop.users.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.forge.blacksmith_shop.users.domain.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
    boolean existsByRoleNameIgnoreCase(String roleName);
    boolean existsByRoleNameIgnoreCaseAndRoleIdNot(String roleName, Integer roleId);

}
