package ru.forge.blacksmith_shop.users.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsersUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public UsersUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> opt = users.findByLogin(username);
        User u = opt.orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Spring Security ожидает префикс ROLE_
        String roleName = u.getRole() != null ? u.getRole().getRoleName() : "User";
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getLogin())
                .password(u.getPasswordHash()) // уже захеширован
                .authorities(List.of(authority))
                .accountExpired(false).accountLocked(false)
                .credentialsExpired(false).disabled(false)
                .build();
    }
}
