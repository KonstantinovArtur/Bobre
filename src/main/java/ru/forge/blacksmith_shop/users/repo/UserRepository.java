// src/main/java/ru/forge/blacksmith_shop/users/repo/UserRepository.java
package ru.forge.blacksmith_shop.users.repo;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.dto.UserRow;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);


    boolean existsByLogin(String login);
    boolean existsByEmail(String email);

    @Query("select (count(u) > 0) from User u where lower(u.login) = lower(:login)")
    boolean existsLoginCi(@Param("login") String login);

    @Query("select (count(u) > 0) from User u where lower(u.email) = lower(:email)")
    boolean existsEmailCi(@Param("email") String email);

    boolean existsByLoginIgnoreCase(String login);
    boolean existsByEmailIgnoreCase(String email);
    long countByRole_RoleId(Integer roleId);
    // для профиля (логин уникален среди «остальных»)
    boolean existsByLoginIgnoreCaseAndUserIdNot(String login, Integer userId);

    @Query("""
    select new ru.forge.blacksmith_shop.users.dto.UserRow(
      u.userId, u.login, u.email, r.roleName, u.createdAt
    )
    from User u
      join u.role r
    order by u.createdAt desc, u.userId desc
  """)
    List<UserRow> findAllRows();
}
