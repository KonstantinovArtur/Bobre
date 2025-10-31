// src/main/java/ru/forge/blacksmith_shop/catalog/web/ManagerProductController.java
package ru.forge.blacksmith_shop.catalog.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.domain.ProductImage;
import ru.forge.blacksmith_shop.catalog.dto.ProductForm;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductImageRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manager/products")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ManagerProductController {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final ProductImageRepository images;

    public ManagerProductController(ProductRepository products,
                                    CategoryRepository categories,
                                    ProductImageRepository images) {
        this.products = products;
        this.categories = categories;
        this.images = images;
    }

    // ---- LIST ----
    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "ok", required = false) String ok) {
        List<Product> all = products.findAll();
        model.addAttribute("products", all);
        model.addAttribute("ok", ok);
        return "manager/products/list";
    }

    // ---- CREATE FORM ----
    @GetMapping("/new")
    public String createForm(Model model) {
        ProductForm form = new ProductForm();
        form.setPromotional(Boolean.FALSE);
        form.setDiscountPercent(java.math.BigDecimal.ZERO);
        form.setStockQty(0);

        model.addAttribute("form", form);
        model.addAttribute("categories", categories.findAll());
        return "manager/products/form";
    }

    // ---- CREATE ----
    @PostMapping("/create")
    @Transactional
    public String create(@ModelAttribute ProductForm form,
                         @RequestParam(name = "image", required = false) MultipartFile image,
                         RedirectAttributes ra) {
        Product p = new Product();
        apply(p, form);
        products.save(p);

        savePrimaryImageIfPresent(p, image);

        ra.addAttribute("ok", "Товар создан");
        return "redirect:/manager/products";
    }

    // ---- EDIT FORM ----
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<Product> opt = products.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("ok", "Товар не найден");
            return "redirect:/manager/products";
        }
        Product product = opt.get();
        ProductForm form = toForm(product);

        model.addAttribute("form", form);
        model.addAttribute("categories", categories.findAll());
        // для предпросмотра текущей картинки
        model.addAttribute("hasImage", images.findPrimaryByProductId(id).isPresent());
        return "manager/products/form";
    }

    // ---- UPDATE ----
    @PostMapping("/{id}/update")
    @Transactional
    public String update(@PathVariable Integer id,
                         @ModelAttribute ProductForm form,
                         @RequestParam(name = "image", required = false) MultipartFile image,
                         RedirectAttributes ra) {
        Product p = products.findById(id).orElse(null);
        if (p == null) {
            ra.addAttribute("ok", "Товар не найден");
            return "redirect:/manager/products";
        }
        form.setId(id);
        apply(p, form);
        products.save(p);

        // если прислали новый файл — сделаем его главным
        savePrimaryImageIfPresent(p, image);

        ra.addAttribute("ok", "Изменения сохранены");
        return "redirect:/manager/products";
    }

    // ---- DELETE ----
    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (products.existsById(id)) {
            products.deleteById(id);
            ra.addAttribute("ok", "Товар удалён");
        } else {
            ra.addAttribute("ok", "Товар не найден");
        }
        return "redirect:/manager/products";
    }

    // ---- helpers ----
    private void apply(Product p, ProductForm f) {
        p.setName(nullToEmpty(f.getName()));
        p.setDescription(nullToEmpty(f.getDescription()));
        p.setPrice(defaultBig(f.getPrice(), java.math.BigDecimal.ZERO));
        p.setStockQty(defaultInt(f.getStockQty(), 0));
        p.setDiscountPercent(defaultBig(f.getDiscountPercent(), java.math.BigDecimal.ZERO));
        p.setPromotional(Boolean.TRUE.equals(f.getPromotional()));

        if (f.getCategoryId() != null) {
            Category c = categories.findById(f.getCategoryId()).orElse(null);
            p.setCategory(c);
        } else {
            p.setCategory(null);
        }
    }

    private ProductForm toForm(Product p) {
        ProductForm f = new ProductForm();
        f.setId(p.getId());
        f.setName(p.getName());
        f.setDescription(p.getDescription());
        f.setPrice(p.getPrice());
        f.setStockQty(p.getStockQty());
        f.setDiscountPercent(p.getDiscountPercent());
        f.setPromotional(p.isPromotional());
        f.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
        return f;
    }

    private void savePrimaryImageIfPresent(Product p, MultipartFile file) {
        if (file == null || file.isEmpty()) return;

        images.resetPrimary(p.getId());

        ProductImage img = new ProductImage();
        img.setProduct(p);
        img.setPrimary(true);
        img.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
        img.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        try {
            img.setBytes(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл изображения", e);
        }
        images.save(img);
    }

    private static String nullToEmpty(String s){ return s == null ? "" : s; }
    private static java.math.BigDecimal defaultBig(java.math.BigDecimal v, java.math.BigDecimal d){ return v == null ? d : v; }
    private static Integer defaultInt(Integer v, Integer d){ return v == null ? d : v; }
}
