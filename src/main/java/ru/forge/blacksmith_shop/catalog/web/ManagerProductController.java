// src/main/java/ru/forge/blacksmith_shop/catalog/web/ManagerProductController.java
package ru.forge.blacksmith_shop.catalog.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import ru.forge.blacksmith_shop.catalog.service.ProductInUseException;
import ru.forge.blacksmith_shop.catalog.service.ProductService;

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
    private final ProductService productService;

    public ManagerProductController(ProductRepository products,
                                    CategoryRepository categories,
                                    ProductImageRepository images,
                                    ProductService productService) {
        this.products = products;
        this.categories = categories;
        this.images = images;
        this.productService = productService;
    }

    // ---------- LIST ----------
    @GetMapping
    public String list(Model model, @RequestParam(value = "ok", required = false) String ok) {
        model.addAttribute("products", products.findAll());
        model.addAttribute("ok", ok);
        return "manager/products/list";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteAlt(@PathVariable Integer id, RedirectAttributes ra) {
        return delete(id, ra);
    }

    // ---------- CREATE FORM ----------
    @GetMapping("/new")
    public String createForm(Model model) {
        ProductForm form = new ProductForm();
        form.setPromotional(Boolean.FALSE);
        form.setDiscountPercent(java.math.BigDecimal.ZERO);
        form.setStockQty(0);

        model.addAttribute("form", form);
        model.addAttribute("categories", categories.findAll());
        model.addAttribute("images", List.of());
        model.addAttribute("isEdit", false);
        return "manager/products/form";
    }

    // ---------- CREATE ----------
    @PostMapping("/create")
    @Transactional
    public String create(@Valid @ModelAttribute("form") ProductForm form,
                         BindingResult br,
                         @RequestParam(value = "upload", required = false) MultipartFile upload,
                         @RequestParam(value = "makePrimary", required = false, defaultValue = "false") boolean makePrimary,
                         Model model,
                         RedirectAttributes ra) throws IOException {

        if (br.hasErrors()) {
            model.addAttribute("categories", categories.findAll());
            model.addAttribute("images", List.of());
            model.addAttribute("isEdit", false);
            return "manager/products/form";
        }

        Product p = new Product();
        apply(p, form);
        products.save(p);

        if (upload != null && !upload.isEmpty()) {
            ProductImage pi = new ProductImage();
            pi.setProduct(p);
            pi.setFilename(safeFilename(upload.getOriginalFilename()));
            pi.setMimeType(upload.getContentType() != null ? upload.getContentType() : "application/octet-stream");
            pi.setBytes(upload.getBytes());
            pi.setPrimary(makePrimary);
            images.save(pi);

            if (!makePrimary) {
                var list = images.findAllByProductIdOrder(p.getId());
                if (list.size() == 1) {
                    list.get(0).setPrimary(true);
                    images.save(list.get(0));
                }
            }
        }

        ra.addAttribute("ok", "Товар создан");
        return "redirect:/manager/products";
    }

    // ---------- EDIT FORM ----------
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<Product> opt = products.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("ok", "Товар не найден");
            return "redirect:/manager/products";
        }

        ProductForm form = toForm(opt.get());
        var imgs = images.findAllByProductIdOrder(id);

        Integer primaryId = imgs.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .map(ProductImage::getId)
                .orElse(imgs.isEmpty() ? null : imgs.get(0).getId());

        model.addAttribute("form", form);
        model.addAttribute("categories", categories.findAll());
        model.addAttribute("images", imgs);
        model.addAttribute("primaryImageId", primaryId); // <—
        model.addAttribute("isEdit", true);
        return "manager/products/form";
    }





    // ---------- UPDATE ----------
    @PostMapping("/{id}/update")
    @Transactional
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") ProductForm form,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        Product p = products.findById(id).orElse(null);
        if (p == null) {
            ra.addAttribute("ok", "Товар не найден");
            return "redirect:/manager/products";
        }

        form.setId(id);

        if (br.hasErrors()) {
            var imgs = images.findAllByProductIdOrder(id);
            Integer primaryId = imgs.stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .map(ProductImage::getId)
                    .orElse(imgs.isEmpty() ? null : imgs.get(0).getId());

            model.addAttribute("categories", categories.findAll());
            model.addAttribute("images", imgs);
            model.addAttribute("primaryImageId", primaryId); // <—
            model.addAttribute("isEdit", true);
            return "manager/products/form";
        }

        apply(p, form);
        products.save(p);
        ra.addAttribute("ok", "Изменения сохранены");
        return "redirect:/manager/products";
    }


    // ---------- DELETE ----------
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            productService.deleteById(id);
            ra.addFlashAttribute("ok", "Товар удалён.");
        } catch (ProductInUseException e) {
            ra.addFlashAttribute("error",
                    e.getMessage() + "");
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", "Товар не найден.");
        }
        return "redirect:/manager/products";
    }





    // ---------- IMAGES: upload (для edit) ----------
    @PostMapping("/{id}/images/add")
    @Transactional
    public String addImage(@PathVariable Integer id,
                           @RequestParam("upload") MultipartFile upload,
                           @RequestParam(value = "makePrimary", defaultValue = "false") boolean makePrimary,
                           RedirectAttributes ra) throws IOException {
        Product p = products.findById(id).orElse(null);
        if (p == null) {
            ra.addAttribute("ok", "Товар не найден");
            return "redirect:/manager/products";
        }
        if (upload == null || upload.isEmpty()) {
            ra.addAttribute("ok", "Файл не выбран");
            return "redirect:/manager/products/" + id + "/edit";
        }

        ProductImage pi = new ProductImage();
        pi.setProduct(p);
        pi.setFilename(safeFilename(upload.getOriginalFilename()));
        pi.setMimeType(upload.getContentType() != null ? upload.getContentType() : "application/octet-stream");
        pi.setBytes(upload.getBytes());
        pi.setPrimary(makePrimary);
        images.save(pi);

        if (makePrimary) {
            // снять primary со старых
            for (ProductImage other : images.findAllByProductIdOrder(id)) {
                if (!other.getId().equals(pi.getId()) && other.isPrimary()) {
                    other.setPrimary(false);
                    images.save(other);
                }
            }
        } else {
            // если нет ни одной главной — сделать текущую главной
            boolean hasPrimary = images.findAllByProductIdOrder(id).stream().anyMatch(ProductImage::isPrimary);
            if (!hasPrimary) {
                pi.setPrimary(true);
                images.save(pi);
            }
        }

        ra.addAttribute("ok", "Картинка добавлена");
        return "redirect:/manager/products/" + id + "/edit";
    }

    // ---------- IMAGES: set primary ----------
    @PostMapping("/{id}/images/{imageId}/primary")
    @Transactional
    public String makePrimary(@PathVariable Integer id,
                              @PathVariable Integer imageId,
                              RedirectAttributes ra) {
        ProductImage pi = images.findById(imageId).orElse(null);
        if (pi == null || pi.getProduct() == null || !id.equals(pi.getProduct().getId())) {
            ra.addAttribute("ok", "Картинка не найдена");
            return "redirect:/manager/products/" + id + "/edit";
        }
        // снять флаг со всех
        for (ProductImage other : images.findAllByProductIdOrder(id)) {
            if (other.isPrimary()) {
                other.setPrimary(false);
                images.save(other);
            }
        }
        pi.setPrimary(true);
        images.save(pi);

        ra.addAttribute("ok", "Главная картинка обновлена");
        return "redirect:/manager/products/" + id + "/edit";
    }

    // ---------- IMAGES: delete ----------
    @PostMapping("/{id}/images/{imageId}/delete")
    @Transactional
    public String deleteImage(@PathVariable Integer id,
                              @PathVariable Integer imageId,
                              RedirectAttributes ra) {
        ProductImage pi = images.findById(imageId).orElse(null);
        if (pi == null || pi.getProduct() == null || !id.equals(pi.getProduct().getId())) {
            ra.addAttribute("ok", "Картинка не найдена");
            return "redirect:/manager/products/" + id + "/edit";
        }
        boolean wasPrimary = pi.isPrimary();
        images.delete(pi);

        if (wasPrimary) {
            // если удалили главную — сделать первой по списку главной
            var rest = images.findAllByProductIdOrder(id);
            if (!rest.isEmpty() && rest.stream().noneMatch(ProductImage::isPrimary)) {
                rest.get(0).setPrimary(true);
                images.save(rest.get(0));
            }
        }

        ra.addAttribute("ok", "Картинка удалена");
        return "redirect:/manager/products/" + id + "/edit";
    }

    // ---------- IMAGES: preview (миниатюры по imageId) ----------
    @GetMapping("/images/{imageId}/preview")
    @ResponseBody
    public ResponseEntity<byte[]> preview(@PathVariable Integer imageId) {
        ProductImage pi = images.findById(imageId).orElse(null);
        if (pi == null || pi.getBytes() == null) {
            return ResponseEntity.notFound().build();
        }
        String mime = (pi.getMimeType() != null && pi.getMimeType().startsWith("image/"))
                ? pi.getMimeType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mime)
                .body(pi.getBytes());
    }

    // ---------- helpers ----------
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

    private static String nullToEmpty(String s){ return s == null ? "" : s; }
    private static java.math.BigDecimal defaultBig(java.math.BigDecimal v, java.math.BigDecimal d){ return v == null ? d : v; }
    private static Integer defaultInt(Integer v, Integer d){ return v == null ? d : v; }

    private static String safeFilename(String original) {
        if (original == null) return "upload.bin";
        String s = original.replace("\\", "/");
        int idx = s.lastIndexOf('/');
        if (idx >= 0) s = s.substring(idx + 1);
        return s.replaceAll("[\\r\\n\\t]", "_");
    }
}
