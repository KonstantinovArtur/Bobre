package ru.forge.blacksmith_shop.catalog.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.catalog.service.ReviewService;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Controller
@RequestMapping("/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepo;

    public ReviewController(ReviewService reviewService, UserRepository userRepo) {
        this.reviewService = reviewService;
        this.userRepo = userRepo;
    }

    @PostMapping("/save")
    public String save(@PathVariable Integer productId,
                       @RequestParam Integer rating,
                       @RequestParam(required = false) String comment,
                       Authentication auth,
                       RedirectAttributes ra) {

        if (auth == null) {
            ra.addFlashAttribute("error", "Авторизуйтесь, чтобы оставить отзыв.");
            return "redirect:/login";
        }

        var user = userRepo.findByLogin(auth.getName()).orElseThrow();
        reviewService.createOrUpdate(user.getUserId(), productId, rating, comment);
        ra.addFlashAttribute("ok", "Отзыв сохранён.");
        return "redirect:/products/" + productId;
    }

    @PostMapping("/delete")
    public String delete(@PathVariable Integer productId,
                         Authentication auth,
                         RedirectAttributes ra) {

        if (auth == null) {
            ra.addFlashAttribute("error", "Авторизуйтесь, чтобы удалить отзыв.");
            return "redirect:/login";
        }

        var user = userRepo.findByLogin(auth.getName()).orElseThrow();
        reviewService.delete(user.getUserId(), productId);
        ra.addFlashAttribute("ok", "Отзыв удалён.");
        return "redirect:/products/" + productId;
    }
}
