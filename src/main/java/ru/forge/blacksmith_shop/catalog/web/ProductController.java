package ru.forge.blacksmith_shop.catalog.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.forge.blacksmith_shop.catalog.repo.ProductCatalogViewRepository;
import ru.forge.blacksmith_shop.catalog.service.ReviewService;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.catalog.domain.Review;

@Controller
public class ProductController {

    private final ProductCatalogViewRepository viewRepo;
    private final ReviewService reviewService;
    private final UserRepository userRepo;

    public ProductController(ProductCatalogViewRepository viewRepo,
                             ReviewService reviewService,
                             UserRepository userRepo) {
        this.viewRepo = viewRepo;
        this.reviewService = reviewService;
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String home() { return "redirect:/products"; }

    @GetMapping("/products")
    public String list(Model model) {
        model.addAttribute("items", viewRepo.findAll());
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
