package ru.forge.blacksmith_shop.catalog.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.forge.blacksmith_shop.catalog.repo.ProductCatalogViewRepository;
import ru.forge.blacksmith_shop.catalog.service.ReviewService;

@Controller
public class ProductController {

    private final ProductCatalogViewRepository viewRepo;
    private final ReviewService reviewService;

    public ProductController(ProductCatalogViewRepository viewRepo,
                             ReviewService reviewService) {
        this.viewRepo = viewRepo;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String home() { return "redirect:/products"; }

    @GetMapping("/products")
    public String list(Model model) {
        model.addAttribute("items", viewRepo.findAll());
        return "catalog/list";
    }

    @GetMapping("/products/{id}")
    public String item(@PathVariable Integer id, Model model) {
        var item = viewRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var reviews = reviewService.byProduct(id);

        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);
        return "catalog/item";
    }
}
