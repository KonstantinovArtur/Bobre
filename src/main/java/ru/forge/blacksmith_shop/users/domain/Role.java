package ru.forge.blacksmith_shop.users.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @NotBlank(message = "Название роли не может быть пустым")
    @Size(min = 3, max = 20, message = "Название роли должно быть от 3 до 20 символов")
    @Pattern(regexp = "^[A-Za-zА-Яа-я][A-Za-zА-Яа-я0-9_-]*$",
            message = "Роль должна начинаться с буквы и содержать только буквы, цифры, дефис или подчёркивание")
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    public Role() {
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(roleId, role.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId);
    }

    @Override
    public String toString() {
        return roleName;
    }
}
