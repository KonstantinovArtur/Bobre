package ru.forge.blacksmith_shop.users.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public class RegisterForm {

    @NotBlank(message = "Укажите логин")
    @Length(min = 3, max = 100, message = "Логин: от 3 до 100 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-я][A-Za-zА-Яа-я0-9_]*$",
            message = "Логин должен начинаться с буквы и содержать только буквы, цифры или _"
    )
    private String login;

    @NotBlank(message = "Укажите email")
    @Email(message = "Введите корректный email")
    @Length(max = 255, message = "Email слишком длинный")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Length(min = 6, max = 72, message = "Пароль: от 6 до 72 символов")
    private String password;

    @NotBlank(message = "Повторите пароль")
    @Length(min = 6, max = 72, message = "Подтверждение пароля: от 6 до 72 символов")
    private String confirm;

    @Length(max = 150, message = "Имя слишком длинное")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё\\s-]*$",
            message = "Имя может содержать только буквы, пробелы и дефисы"
    )
    private String name;

    public RegisterForm() {}

    // Getters / Setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirm() { return confirm; }
    public void setConfirm(String confirm) { this.confirm = confirm; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Проверка совпадения паролей
    @AssertTrue(message = "Пароли не совпадают")
    public boolean isPasswordsMatch() {
        if (password == null || confirm == null) return true;
        return password.equals(confirm);
    }
}
