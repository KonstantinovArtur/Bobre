// src/main/java/.../users/dto/RoleNameForm.java
package ru.forge.blacksmith_shop.users.dto;

import jakarta.validation.constraints.*;

public class RoleNameForm {
    @NotBlank(message = "Название роли не может быть пустым")
    @Size(min = 3, max = 20, message = "Название роли должно быть от 3 до 20 символов")
    @Pattern(regexp = "^[A-Za-zА-Яа-я][A-Za-zА-Яа-я0-9_-]*$",
            message = "Роль должна начинаться с буквы и содержать только буквы, цифры, дефис или подчёркивание")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
