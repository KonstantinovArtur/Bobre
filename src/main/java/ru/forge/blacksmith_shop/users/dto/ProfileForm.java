// src/main/java/ru/forge/blacksmith_shop/users/dto/ProfileForm.java
package ru.forge.blacksmith_shop.users.dto;

import jakarta.validation.constraints.*;

public class ProfileForm {

    @NotBlank
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_.-]*$", message = "Логин должен начинаться с буквы и содержать только буквы, цифры, _ . -")
    private String login;

    @Size(min = 3, max = 150, message = "Имя должно содержать минимум 3 символа")
    @Pattern(
            regexp = "^(?=\\p{L})[\\p{L}\\p{M}\\-\\s]{3,150}$",
            message = "Имя должно начинаться с буквы и содержать только буквы, пробелы и дефисы"
    )
    private String name;

    public ProfileForm() {}
    public ProfileForm(String login, String name) { this.login = login; this.name = name; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
