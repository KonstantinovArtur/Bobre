package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "Products", description = "CRUD и поиск товаров кузницы")
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductApiController {

    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    public ProductApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ===== DTOs =====

    public record ProductDto(
            Integer id,
            String name,
            BigDecimal price,
            Integer stockQty,
            Integer discountPercent,
            Boolean isPromotional,
            Integer categoryId,
            String categoryName
    ) {}

    public static class UpsertProductDto {
        @NotBlank(message = "Название обязательно")
        public String name;

        @NotNull(message = "Цена обязательна")
        @Min(value = 0, message = "Цена должна быть неотрицательной")
        public BigDecimal price;

        @NotNull(message = "Остаток обязателен")
        @Min(value = 0, message = "Остаток не может быть отрицательным")
        public Integer stockQty;

        @NotNull(message = "Скидка обязательна")
        @Min(value = 0, message = "Скидка должна быть от 0 до 100")
        @Max(value = 100, message = "Скидка должна быть от 0 до 100")
        public Integer discountPercent;

        /** Можно не передавать — по умолчанию false */
        public Boolean isPromotional;

        /** Можно не передавать */
        public Integer categoryId;

        /** Опционально — если есть такое поле в сущности */
        public String description;
    }

    private ProductDto toDto(Product p) {
        Integer catId = null;
        String catName = null;
        if (p.getCategory() != null) {
            catId = p.getCategory().getId();
            catName = p.getCategory().getName();
        }
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getStockQty(),
                safeGetDiscount(p),
                safeGetIsPromotional(p),
                catId,
                catName
        );
    }

    // ===== READ =====

    @Operation(summary = "Список товаров")
    @GetMapping
    public List<ProductDto> getAll() {
        return productRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "Получить товар по ID")
    @GetMapping("/{id}")
    public ProductDto getOne(@PathVariable Integer id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден: " + id));
        return toDto(p);
    }

    @Operation(summary = "Поиск товаров по названию и категории")
    @GetMapping("/search")
    public List<ProductDto> search(@RequestParam(required = false) String q,
                                   @RequestParam(required = false, name = "catId") Integer categoryId) {
        if (hasSearch(productRepository)) {
            // если есть кастомный метод search(q, categoryId)
            return productRepository.search(q, categoryId)
                    .stream().map(this::toDto).collect(Collectors.toList());
        } else {
            // простой фильтр в памяти (на малых объёмах)
            return productRepository.findAll().stream()
                    .filter(p -> q == null || p.getName() != null && p.getName().toLowerCase().contains(q.toLowerCase()))
                    .filter(p -> categoryId == null || (p.getCategory() != null && Objects.equals(p.getCategory().getId(), categoryId)))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
    }

    // ===== CREATE =====

    @Operation(summary = "Создать товар")
    @PostMapping
    @Transactional
    public ResponseEntity<ProductDto> create(@Valid @RequestBody UpsertProductDto req) {
        Product p = new Product();
        applyUpsert(p, req);
        productRepository.save(p);
        return ResponseEntity.created(URI.create("/api/products/" + p.getId())).body(toDto(p));
    }

    // ===== UPDATE (полная замена) =====

    @Operation(summary = "Обновить товар (PUT)")
    @PutMapping("/{id}")
    @Transactional
    public ProductDto update(@PathVariable Integer id, @Valid @RequestBody UpsertProductDto req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден: " + id));
        applyUpsert(p, req);
        return toDto(p);
    }

    // ===== PATCH (частично) =====

    @Operation(summary = "Частично обновить товар (PATCH)")
    @PatchMapping("/{id}")
    @Transactional
    public ProductDto patch(@PathVariable Integer id, @RequestBody UpsertProductDto req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден: " + id));

        if (req.name != null && !req.name.isBlank()) p.setName(req.name);
        if (req.price != null && req.price.compareTo(BigDecimal.ZERO) >= 0) p.setPrice(req.price);
        if (req.stockQty != null && req.stockQty >= 0) p.setStockQty(req.stockQty);
        if (req.discountPercent != null) setDiscount(p, BigDecimal.valueOf(clamp(req.discountPercent, 0, 100)));
        if (req.isPromotional != null) setPromotionalFlag(p, req.isPromotional);
        if (req.categoryId != null) {
            Category ref = em.getReference(Category.class, req.categoryId);
            p.setCategory(ref);
        }
        if (req.description != null) safeSetDescription(p, req.description);

        return toDto(p);
    }

    // ===== DELETE =====

    @Operation(summary = "Удалить товар")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            productRepository.deleteById(id); // если нет — бросит EmptyResultDataAccessException
            return ResponseEntity.noContent().build();
        } catch (EmptyResultDataAccessException ex) {
            throw new NoSuchElementException("Товар не найден: " + id);
        }
    }

    // ===== Приватные помощники =====

    private void applyUpsert(Product p, UpsertProductDto req) {
        p.setName(req.name);
        p.setPrice(req.price);
        p.setStockQty(req.stockQty);

        int disc = req.discountPercent != null ? clamp(req.discountPercent, 0, 100) : 0;
        setDiscount(p, BigDecimal.valueOf(disc));   // <-- важно

        boolean promo = req.isPromotional != null ? req.isPromotional : false;
        setPromotionalFlag(p, promo);

        if (req.categoryId != null) {
            Category ref = em.getReference(Category.class, req.categoryId);
            p.setCategory(ref);
        }
        if (req.description != null) {
            safeSetDescription(p, req.description);
        }
    }


    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ----- discountPercent -----

    // ----- discount / discountPercent -----

    // ----- discount / discountPercent -----

    private void setDiscount(Product p, BigDecimal discount) {
        // setDiscountPercent(BigDecimal)
        try { p.getClass().getMethod("setDiscountPercent", BigDecimal.class).invoke(p, discount); return; }
        catch (NoSuchMethodException ignore) {} catch (Exception ignore) {}

        // setDiscount(BigDecimal)
        try { p.getClass().getMethod("setDiscount", BigDecimal.class).invoke(p, discount); return; }
        catch (NoSuchMethodException ignore) {} catch (Exception ignore) {}

        // запасные варианты — если в сущности int/Integer
        try { p.getClass().getMethod("setDiscountPercent", Integer.class).invoke(p, discount.intValue()); return; }
        catch (NoSuchMethodException ignore) {} catch (Exception ignore) {}

        try { p.getClass().getMethod("setDiscountPercent", int.class).invoke(p, discount.intValue()); return; }
        catch (NoSuchMethodException ignore) {} catch (Exception ignore) {}

        try { p.getClass().getMethod("setDiscount", Integer.class).invoke(p, discount.intValue()); return; }
        catch (NoSuchMethodException ignore) {} catch (Exception ignore) {}

        try { p.getClass().getMethod("setDiscount", int.class).invoke(p, discount.intValue()); }
        catch (Exception ignore) {}
    }

    private Integer safeGetDiscount(Product p) {
        // getDiscountPercent()
        try {
            Object v = p.getClass().getMethod("getDiscountPercent").invoke(p);
            if (v instanceof BigDecimal bd) return bd.intValue();
            if (v instanceof Number n)     return n.intValue();
        } catch (NoSuchMethodException ignore) { } catch (Exception ignore) { }

        // getDiscount()
        try {
            Object v = p.getClass().getMethod("getDiscount").invoke(p);
            if (v instanceof BigDecimal bd) return bd.intValue();
            if (v instanceof Number n)     return n.intValue();
        } catch (Exception ignore) { }

        return null;
    }



    // ----- promotional -----

    private void setPromotionalFlag(Product p, boolean val) {
        // Основной кейс: setPromotional(boolean)
        try {
            var m = p.getClass().getMethod("setPromotional", boolean.class);
            m.invoke(p, val);
            return;
        } catch (NoSuchMethodException ignore) { } catch (Exception ignore) { }

        // Запасной кейс: setIsPromotional(Boolean/boolean)
        try {
            var m = p.getClass().getMethod("setIsPromotional", Boolean.class);
            m.invoke(p, val);
            return;
        } catch (NoSuchMethodException ignore) { } catch (Exception ignore) { }
        try {
            var m = p.getClass().getMethod("setIsPromotional", boolean.class);
            m.invoke(p, val);
        } catch (Exception ignore) { }
    }

    private Boolean safeGetIsPromotional(Product p) {
        // Основной геттер JavaBeans для boolean — isPromotional()
        try {
            var m = p.getClass().getMethod("isPromotional");
            Object val = m.invoke(p);
            return (val instanceof Boolean) ? (Boolean) val : null;
        } catch (NoSuchMethodException ignore) { } catch (Exception ignore) { }

        // Альтернативный вариант — getPromotional()
        try {
            var m = p.getClass().getMethod("getPromotional");
            Object val = m.invoke(p);
            return (val instanceof Boolean) ? (Boolean) val : null;
        } catch (Exception ignore) { return null; }
    }

    // ----- description -----

    private void safeSetDescription(Product p, String description) {
        try {
            var m = p.getClass().getMethod("setDescription", String.class);
            m.invoke(p, description);
        } catch (Exception ignore) { }
    }

    // ----- вспомогательное -----

    private boolean hasSearch(ProductRepository repo) {
        try {
            repo.getClass().getMethod("search", String.class, Integer.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
