package ru.forge.blacksmith_shop.users.dto;

import jakarta.validation.constraints.*;

public class RegisterForm {

    @NotBlank @Size(min=3, max=100)
    private String login;

    @NotBlank @Email @Size(max=255)
    private String email;

    @NotBlank @Size(min=6, max=72)
    private String password;

    @NotBlank @Size(min=6, max=72)
    private String confirm;

    @Size(max=150)
    private String name;

    public RegisterForm() {}

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
}
