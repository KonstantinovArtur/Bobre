// src/main/java/ru/forge/blacksmith_shop/users/dto/AdminCreateUserForm.java
package ru.forge.blacksmith_shop.users.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public class AdminCreateUserForm {

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
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email должен содержать '@' и заканчиваться корректным доменом (.ru, .com, .org и т.д.)"
    )
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Length(min = 6, max = 72, message = "Пароль: от 6 до 72 символов")
    private String password;

    @Length(max = 150, message = "Имя слишком длинное")
    @Pattern(
            regexp = "^(?=\\p{L})[\\p{L}\\p{M}\\-\\s]{3,150}$",
            message = "Имя должно начинаться с буквы и содержать только буквы, пробелы и дефисы, и быть длинее 3 символов"
    )
    private String name;

    @NotNull(message = "Укажите роль")
    private Integer roleId;

    // getters/setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login == null ? null : login.trim(); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null ? null : name.trim()); }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}
