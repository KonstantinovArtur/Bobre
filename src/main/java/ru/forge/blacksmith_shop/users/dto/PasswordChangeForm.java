// src/main/java/ru/forge/blacksmith_shop/users/dto/PasswordChangeForm.java
package ru.forge.blacksmith_shop.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeForm {

    @NotBlank
    private String currentPassword;

    @NotBlank @Size(min = 6, max = 72)
    private String newPassword;

    @NotBlank @Size(min = 6, max = 72)
    private String confirm;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirm() { return confirm; }
    public void setConfirm(String confirm) { this.confirm = confirm; }
}
