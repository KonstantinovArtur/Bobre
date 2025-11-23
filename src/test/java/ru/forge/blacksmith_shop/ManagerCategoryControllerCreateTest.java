package ru.forge.blacksmith_shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.service.CategoryService;
import ru.forge.blacksmith_shop.catalog.web.ManagerCategoryController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManagerCategoryController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ManagerCategoryControllerCreateTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CategoryRepository categories;

    @MockBean
    CategoryService categoryService; // нужен для создания контроллера, в create() не используется

    // ---------- 1. УСПЕШНОЕ СОЗДАНИЕ ----------
    @Test
    void createCategory_Success() throws Exception {
        String name = "Новая категория";

        when(categories.existsByNameIgnoreCase(eq(name))).thenReturn(false);
        when(categories.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1);
            return c;
        });

        mockMvc.perform(
                        post("/manager/categories/create")
                                .param("name", name)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute("ok", "Категория создана"));

        verify(categories).existsByNameIgnoreCase(name);
        verify(categories).save(any(Category.class));
    }

    // ---------- 2. ОШИБКА ВАЛИДАЦИИ (Pattern) ----------
    @Test
    void createCategory_ValidationError_NamePattern() throws Exception {
        // "1ab" - длина 3, но начинается с цифры -> падает @Pattern, а не @Size
        String badName = "1ab";

        mockMvc.perform(
                        post("/manager/categories/create")
                                .param("name", badName)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute(
                        "error",
                        "Название должно начинаться с буквы и содержать только буквы, цифры, пробелы и дефисы"
                ));

        // из-за ошибок валидации репозиторий не должен вызываться
        verify(categories, never()).existsByNameIgnoreCase(any());
        verify(categories, never()).save(any());
    }

    // ---------- 3. КАТЕГОРИЯ УЖЕ СУЩЕСТВУЕТ ----------
    @Test
    void createCategory_AlreadyExists_Error() throws Exception {
        String name = "Игры";

        when(categories.existsByNameIgnoreCase(eq(name))).thenReturn(true);

        mockMvc.perform(
                        post("/manager/categories/create")
                                .param("name", name)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute(
                        "error",
                        "Категория «" + name + "» уже существует"
                ));

        verify(categories).existsByNameIgnoreCase(name);
        verify(categories, never()).save(any());
    }

    // ---------- 4. DataIntegrityViolation при сохранении ----------
    @Test
    void createCategory_DataIntegrityViolation_Error() throws Exception {
        String name = "Игрушки";

        when(categories.existsByNameIgnoreCase(eq(name))).thenReturn(false);
        when(categories.save(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(
                        post("/manager/categories/create")
                                .param("name", name)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute(
                        "error",
                        "Категория с таким названием уже существует"
                ));

        verify(categories).existsByNameIgnoreCase(name);
        verify(categories).save(any(Category.class));
    }
}
