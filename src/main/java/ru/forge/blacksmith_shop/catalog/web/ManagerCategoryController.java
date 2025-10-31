// src/main/java/ru/forge/blacksmith_shop/manager/ManagerCategoryController.java
package ru.forge.blacksmith_shop.catalog.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.dto.CategoryForm;

import java.util.Optional;

@Controller
@RequestMapping("/manager/categories")
public class ManagerCategoryController {

    private final CategoryRepository categories;

    public ManagerCategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    @GetMapping
    public String list(Model model, @RequestParam(value = "ok", required = false) String ok) {
        model.addAttribute("items", categories.findAll());
        model.addAttribute("ok", ok);
        return "manager/categories/list";
    }

    @PostMapping("/create")
    @Transactional
    public String create(@RequestParam("name") String name, RedirectAttributes ra) {
        Category c = new Category();
        c.setName(name == null ? "" : name.trim());
        categories.save(c);
        ra.addAttribute("ok", "Категория создана");
        return "redirect:/manager/categories";
    }

    @PostMapping("/{id}/update")
    @Transactional
    public String update(@PathVariable Integer id, @RequestParam("name") String name, RedirectAttributes ra) {
        Optional<Category> opt = categories.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("ok", "Категория не найдена");
            return "redirect:/manager/categories";
        }
        Category c = opt.get();
        c.setName(name == null ? "" : name.trim());
        categories.save(c);
        ra.addAttribute("ok", "Изменения сохранены");
        return "redirect:/manager/categories";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (categories.existsById(id)) {
            categories.deleteById(id);
            ra.addAttribute("ok", "Категория удалена");
        } else {
            ra.addAttribute("ok", "Категория не найдена");
        }
        return "redirect:/manager/categories";
    }
}
