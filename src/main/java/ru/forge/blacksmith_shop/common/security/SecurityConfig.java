// src/main/java/ru/forge/blacksmith_shop/common/security/SecurityConfig.java
package ru.forge.blacksmith_shop.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.forge.blacksmith_shop.cart.CartLogoutHandler;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private CartLogoutHandler cartLogoutHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UserDetailsService uds,
                                           BCryptPasswordEncoder encoder) throws Exception {

        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        http.authenticationProvider(provider);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/products/**", "/images/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/register", "/login", "/error").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("Admin")
                        .requestMatchers("/manager/**").hasAnyRole("Admin", "Manager")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("login")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(cartLogoutHandler)  // <- сохраняем корзину ДО инвалидирования сессии
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/"));

        return http.build();
    }
}
