package ru.forge.blacksmith_shop.catalog.web;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductCatalogViewRepository;
import ru.forge.blacksmith_shop.catalog.service.ReviewService;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.catalog.domain.Review;

@Controller
public class ProductController {

    private final ProductCatalogViewRepository viewRepo;
    private final ReviewService reviewService;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ProductController(ProductCatalogViewRepository viewRepo,
                             ReviewService reviewService,
                             UserRepository userRepo, CategoryRepository categoryRepo) {
        this.viewRepo = viewRepo;
        this.reviewService = reviewService;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping("/")
    public String home() { return "redirect:/products"; }

    @GetMapping("/products")
    public String catalog(@RequestParam(value = "q", required = false) String q,
                          @RequestParam(value = "cat", required = false) Integer cat,
                          Model model) {

        // нормализуем параметры
        String qNorm = (q == null || q.isBlank()) ? null : q.trim();
        Integer catNorm = (cat == null || cat == 0) ? null : cat;

        var items = (qNorm == null && catNorm == null)
                ? viewRepo.findAllByOrderByNameAsc()
                : viewRepo.search(qNorm, catNorm);

        // категории для выпадающего списка (отсортированные по имени)
        var categories = categoryRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));

        // отдадим в шаблон исходные значения формы, чтобы они не терялись
        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("cat", cat); // может быть null

        return "catalog/list";
    }

    @GetMapping("/products/{id}")
    public String item(@PathVariable Integer id, Model model, Authentication auth) {
        var item = viewRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var reviews = reviewService.byProduct(id);
        model.addAttribute("reviews", reviews);

        Integer myUserId = null;
        if (auth != null && auth.isAuthenticated()) {
            myUserId = userRepo.findByLogin(auth.getName()).map(u -> u.getUserId()).orElse(null);
        }

        Review myReview = (myUserId == null)
                ? null
                : reviewService.findUserReview(myUserId, id).orElse(null);

        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);
        model.addAttribute("myReview", myReview);
        return "catalog/item";
    }
}