// src/main/java/ru/forge/blacksmith_shop/catalog/web/ManagerCategoryController.java
package ru.forge.blacksmith_shop.catalog.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.dto.CategoryForm;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.service.CategoryInUseException;
import ru.forge.blacksmith_shop.catalog.service.CategoryService;

@Controller
@RequestMapping("/manager/categories")
public class ManagerCategoryController {

    private final CategoryRepository categories;
    private final CategoryService categoryService;

    // ✅ передаём ОБЕ зависимости в конструктор
    public ManagerCategoryController(CategoryRepository categories,
                                     CategoryService categoryService) {
        this.categories = categories;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "ok", required = false) String ok,
                       @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("items", categories.findAll());
        if (ok != null) model.addAttribute("ok", ok);
        if (error != null) model.addAttribute("error", error);

        if (!model.containsAttribute("createForm")) {
            model.addAttribute("createForm", new CategoryForm());
        }
        if (!model.containsAttribute("editForm")) {
            model.addAttribute("editForm", new CategoryForm());
        }
        return "manager/categories/list";
    }

    // Создание
    @PostMapping("/create")
    @Transactional
    public String create(@Valid CategoryForm form,
                         BindingResult br,
                         RedirectAttributes ra) {
        String name = form.getName() == null ? "" : form.getName().trim();

        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/manager/categories";
        }
        if (categories.existsByNameIgnoreCase(name)) {
            ra.addFlashAttribute("error", "Категория «" + name + "» уже существует");
            return "redirect:/manager/categories";
        }

        try {
            Category c = new Category();
            c.setName(name);
            categories.save(c);
            ra.addFlashAttribute("ok", "Категория создана");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "Категория с таким названием уже существует");
        }
        return "redirect:/manager/categories";
    }

    // Обновление
    @PostMapping("/{id}/update")
    @Transactional
    public String update(@PathVariable Integer id,
                         @Valid CategoryForm form,
                         BindingResult br,
                         RedirectAttributes ra) {
        Category c = categories.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Категория не найдена");
            return "redirect:/manager/categories";
        }

        String name = form.getName() == null ? "" : form.getName().trim();

        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/manager/categories";
        }
        if (!name.equalsIgnoreCase(c.getName()) && categories.existsByNameIgnoreCase(name)) {
            ra.addFlashAttribute("error", "Категория «" + name + "» уже существует");
            return "redirect:/manager/categories";
        }

        try {
            c.setName(name);
            categories.save(c);
            ra.addFlashAttribute("ok", "Изменения сохранены");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "Категория с таким названием уже существует");
        }
        return "redirect:/manager/categories";
    }

    // Удаление с защитой
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("ok", "Категория удалена.");
        } catch (CategoryInUseException e) {
            ra.addFlashAttribute("error",
                    "Нельзя удалить категорию: к ней привязано " + e.getCount()
                            + " товар(ов). Сначала перенесите/удалите эти товары.");
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", "Категория не найдена.");
        }
        return "redirect:/manager/categories";
    }
}
