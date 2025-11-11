// src/main/java/ru/forge/blacksmith_shop/api/controllers/AdminReviewApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.catalog.domain.Review;
import ru.forge.blacksmith_shop.catalog.dto.ReviewDto;
import ru.forge.blacksmith_shop.catalog.repo.ReviewRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Admin Reviews", description = "Админ/модерация отзывов")
@RestController
@RequestMapping("/api/admin/reviews")
@Validated
public class AdminReviewApiController {

    private final ReviewRepository reviews;

    public AdminReviewApiController(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    @Operation(summary = "Все отзывы по товару")
    @GetMapping("/product/{productId}")
    public List<ReviewDto> byProduct(@PathVariable Integer productId) {
        return reviews.findAllByProduct_Id(productId).stream()
                .map(r -> new ReviewDto(
                        r.getReviewId(),
                        r.getUser().getLogin(),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    @Operation(summary = "Удалить отзыв по reviewId")
    @DeleteMapping("/{reviewId}")
    @Transactional
    public ResponseEntity<?> deleteById(@PathVariable Integer reviewId) {
        Review r = reviews.findById(reviewId).orElseThrow(() -> new NoSuchElementException("Отзыв не найден"));
        reviews.delete(r);
        return ResponseEntity.noContent().build();
    }
}
