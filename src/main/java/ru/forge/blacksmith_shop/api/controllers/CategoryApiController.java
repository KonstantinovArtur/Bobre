// src/main/java/ru/forge/blacksmith_shop/api/controllers/CategoryApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.dto.CategoryForm;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Categories", description = "CRUD и поиск категорий кузницы")
@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryApiController {

    private final CategoryRepository categoryRepository;

    public CategoryApiController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // --- READ ALL ---
    @Operation(summary = "Список всех категорий")
    @GetMapping
    public List<Category> list() {
        return categoryRepository.findAll();
    }

    // --- READ ONE ---
    @Operation(summary = "Получить категорию по ID")
    @GetMapping("/{id}")
    public Category getOne(@PathVariable Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Категория id=" + id + " не найдена"));
    }

    // --- CREATE ---
    @Operation(summary = "Создать категорию")
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@Valid @RequestBody CategoryForm form) {
        // проверка уникальности имени (без учёта регистра)
        if (categoryRepository.existsByNameIgnoreCase(form.getName())) {
            return ResponseEntity.status(409).body("Категория с именем \"" + form.getName() + "\" уже существует");
        }
        Category c = new Category();
        c.setName(form.getName().trim());
        Category saved = categoryRepository.saveAndFlush(c);
        return ResponseEntity.created(URI.create("/api/categories/" + saved.getId()))
                .body(saved);
    }

    // --- UPDATE ---
    @Operation(summary = "Обновить название категории")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody CategoryForm form) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Категория id=" + id + " не найдена"));


        var sameName = categoryRepository.findByNameIgnoreCase(form.getName().trim());
        if (sameName.isPresent() && !sameName.get().getId().equals(id)) {
            return ResponseEntity.status(409).body("Имя \"" + form.getName() + "\" занято другой категорией");
        }

        existing.setName(form.getName().trim());
        return ResponseEntity.ok(categoryRepository.saveAndFlush(existing));
    }

    // --- DELETE ---
    @Operation(summary = "Удалить категорию по ID")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Категория id=" + id + " не найдена"));
        try {
            categoryRepository.delete(existing);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            // вероятно, есть связанные товары (FK)
            return ResponseEntity.status(409).body("Нельзя удалить: на категорию ссылаются другие записи");
        }
    }


}
