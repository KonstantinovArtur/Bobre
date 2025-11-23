package ru.forge.blacksmith_shop;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.forge.blacksmith_shop.users.dto.RegisterForm;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validForm_PassesValidation() {
        RegisterForm form = new RegisterForm();
        form.setLogin("User123");
        form.setEmail("user@example.com");
        form.setPassword("Secret123");
        form.setConfirm("Secret123");
        form.setName("Иван Иванов");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty(), "Ожидали отсутствие ошибок валидации");
    }

    @Test
    void loginTooShort_FailsValidation() {
        RegisterForm form = new RegisterForm();
        form.setLogin("ab"); // меньше 3 символов
        form.setEmail("user@example.com");
        form.setPassword("Secret123");
        form.setConfirm("Secret123");
        form.setName("Иван Иванов");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().equals("Логин: от 3 до 100 символов")),
                "Ожидали сообщение: 'Логин: от 3 до 100 символов'"
        );
    }

    @Test
    void loginInvalidPattern_FailsValidation() {
        RegisterForm form = new RegisterForm();
        form.setLogin("1user"); // начинается не с буквы
        form.setEmail("user@example.com");
        form.setPassword("Secret123");
        form.setConfirm("Secret123");
        form.setName("Иван Иванов");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v ->
                        v.getMessage().equals("Логин должен начинаться с буквы и содержать только буквы, цифры или _")
                ),
                "Ожидали сообщение о неверном формате логина"
        );
    }

    @Test
    void invalidEmail_FailsValidation() {
        RegisterForm form = new RegisterForm();
        form.setLogin("User123");
        form.setEmail("bad-email"); // некорректный email
        form.setPassword("Secret123");
        form.setConfirm("Secret123");
        form.setName("Иван Иванов");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().equals("Введите корректный email")),
                "Ожидали сообщение: 'Введите корректный email'"
        );
    }

    @Test
    void passwordsNotMatch_FailsAssertTrue() {
        RegisterForm form = new RegisterForm();
        form.setLogin("User123");
        form.setEmail("user@example.com");
        form.setPassword("Secret123");
        form.setConfirm("Other123");  // не совпадает
        form.setName("Иван Иванов");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().equals("Пароли не совпадают")),
                "Ожидали сообщение: 'Пароли не совпадают'"
        );
    }

    @Test
    void nameTooLong_FailsValidation() {
        RegisterForm form = new RegisterForm();
        form.setLogin("User123");
        form.setEmail("user@example.com");
        form.setPassword("Secret123");
        form.setConfirm("Secret123");

        // строка > 150 символов
        form.setName("А".repeat(200));

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().equals("Имя слишком длинное")),
                "Ожидали сообщение: 'Имя слишком длинное'"
        );
    }
}
