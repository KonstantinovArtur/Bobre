package ru.forge.blacksmith_shop;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.forge.blacksmith_shop.users.dto.RegisterForm;
import ru.forge.blacksmith_shop.users.service.AuthService;
import ru.forge.blacksmith_shop.users.web.AuthController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthService auth;

    @Test
    void loginPage_ReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void registerPage_ReturnsRegisterViewWithForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void doRegister_ValidForm_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("login", "User123")
                        .param("email", "user@example.com")
                        .param("password", "Secret123")
                        .param("confirm", "Secret123")
                        .param("name", "Иван Иванов")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(auth).register(any(RegisterForm.class));
    }

    @Test
    void doRegister_BeanValidationErrors_ReturnsRegisterView() throws Exception {
        // специально передаём некорректные данные
        mockMvc.perform(post("/register")
                        .param("login", "")              // пустой логин
                        .param("email", "bad-email")     // кривой email
                        .param("password", "")           // пустой пароль
                        .param("confirm", "")            // пустое подтверждение
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors(
                        "form", "login", "email", "password", "confirm"));

        Mockito.verifyNoInteractions(auth);
    }

    @Test
    void doRegister_LoginErrorFromService_BindsToLoginField() throws Exception {
        doThrow(new IllegalArgumentException("Логин уже занят"))
                .when(auth).register(any(RegisterForm.class));

        mockMvc.perform(post("/register")
                        .param("login", "User123")
                        .param("email", "user@example.com")
                        .param("password", "Secret123")
                        .param("confirm", "Secret123")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("form", "login"));
    }

    @Test
    void doRegister_EmailErrorFromService_BindsToEmailField() throws Exception {
        doThrow(new IllegalArgumentException("Email уже используется"))
                .when(auth).register(any(RegisterForm.class));

        mockMvc.perform(post("/register")
                        .param("login", "User123")
                        .param("email", "user@example.com")
                        .param("password", "Secret123")
                        .param("confirm", "Secret123")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("form", "email"));
    }



    @Test
    void doRegister_OtherErrorFromService_BindsGlobalError() throws Exception {
        doThrow(new IllegalArgumentException("Какая-то другая ошибка"))
                .when(auth).register(any(RegisterForm.class));

        mockMvc.perform(post("/register")
                        .param("login", "User123")
                        .param("email", "user@example.com")
                        .param("password", "Secret123")
                        .param("confirm", "Secret123")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasErrors("form"));
    }
}
