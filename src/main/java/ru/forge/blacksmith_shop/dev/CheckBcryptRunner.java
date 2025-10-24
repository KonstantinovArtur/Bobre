package ru.forge.blacksmith_shop.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CheckBcryptRunner implements CommandLineRunner {

    private final BCryptPasswordEncoder enc;

    public CheckBcryptRunner(BCryptPasswordEncoder enc) {
        this.enc = enc;
    }

    @Override
    public void run(String... args) {
        // Проверим, что пароли совпадают
        String raw = "test";
        String hash = enc.encode(raw);

        System.out.println("Пароль: " + raw);
        System.out.println("Хэш:    " + hash);
        System.out.println("Проверка enc.matches: " + enc.matches("test", hash)); // должно быть true
    }
}
