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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManagerCategoryController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ManagerCategoryControllerUpdateTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CategoryRepository categories;

    @MockBean
    CategoryService categoryService; // нужен для конструктора, в update не используется

    // ---------- 1. УСПЕШНОЕ ИЗМЕНЕНИЕ ----------
    @Test
    void updateCategory_Success() throws Exception {
        Integer id = 10;
        String oldName = "Старая категория";
        String newName = "Новая категория";

        Category existing = new Category();
        existing.setId(id);
        existing.setName(oldName);

        when(categories.findById(id)).thenReturn(Optional.of(existing));
        when(categories.existsByNameIgnoreCase(eq(newName))).thenReturn(false);
        when(categories.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(
                        post("/manager/categories/{id}/update", id)
                                .param("name", newName)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute("ok", "Изменения сохранены"));

        // проверяем, что имя реально изменили и сохранили
        verify(categories).save(argThat(c ->
                c.getId().equals(id) && c.getName().equals(newName)
        ));
    }

    // ---------- 2. ОШИБКА ВАЛИДАЦИИ (Pattern) ----------
    @Test
    void updateCategory_ValidationError_NamePattern() throws Exception {
        Integer id = 11;

        // существующая категория (чтобы не сработала ветка "не найдена")
        Category existing = new Category();
        existing.setId(id);
        existing.setName("Категория");
        when(categories.findById(id)).thenReturn(Optional.of(existing));

        // "1ab" — длина 3, но начинается не с буквы → падает @Pattern, а не @Size/@NotBlank
        String badName = "1ab";

        mockMvc.perform(
                        post("/manager/categories/{id}/update", id)
                                .param("name", badName)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute(
                        "error",
                        "Название должно начинаться с буквы и содержать только буквы, цифры, пробелы и дефисы"
                ));

        verify(categories, never()).existsByNameIgnoreCase(any());
        verify(categories, never()).save(any());
    }

    // ---------- 3. КАТЕГОРИЯ НЕ НАЙДЕНА ----------
    @Test
    void updateCategory_NotFound() throws Exception {
        Integer id = 999;
        when(categories.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/manager/categories/{id}/update", id)
                                .param("name", "Любое имя")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute("error", "Категория не найдена"));

        verify(categories, never()).save(any());
    }

    // ---------- 4. ИМЯ УЖЕ СУЩЕСТВУЕТ ----------
    @Test
    void updateCategory_AlreadyExists_Error() throws Exception {
        Integer id = 12;
        String oldName = "Старая";
        String newName = "Игрушки";

        Category existing = new Category();
        existing.setId(id);
        existing.setName(oldName);

        when(categories.findById(id)).thenReturn(Optional.of(existing));
        // имя изменилось и уже занято
        when(categories.existsByNameIgnoreCase(eq(newName))).thenReturn(true);

        mockMvc.perform(
                        post("/manager/categories/{id}/update", id)
                                .param("name", newName)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute(
                        "error",
                        "Категория «" + newName + "» уже существует"
                ));

        verify(categories).existsByNameIgnoreCase(newName);
        verify(categories, never()).save(any());
    }

    // ---------- 5. DataIntegrityViolation при сохранении ----------
    @Test
    void updateCategory_DataIntegrityViolation_Error() throws Exception {
        Integer id = 13;
        String oldName = "Старая";
        String newName = "Новая";

        Category existing = new Category();
        existing.setId(id);
        existing.setName(oldName);

        when(categories.findById(id)).thenReturn(Optional.of(existing));
        when(categories.existsByNameIgnoreCase(eq(newName))).thenReturn(false);
        when(categories.save(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(
                        post("/manager/categories/{id}/update", id)
                                .param("name", newName)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/categories"))
                .andExpect(flash().attribute(
                        "error",
                        "Категория с таким названием уже существует"
                ));

        verify(categories).existsByNameIgnoreCase(newName);
        verify(categories).save(any(Category.class));
    }
}
