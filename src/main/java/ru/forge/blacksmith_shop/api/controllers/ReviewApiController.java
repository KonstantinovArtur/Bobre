// src/main/java/ru/forge/blacksmith_shop/api/controllers/ReviewApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.domain.Review;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;
import ru.forge.blacksmith_shop.catalog.repo.ReviewRepository;
import ru.forge.blacksmith_shop.users.domain.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Reviews", description = "Отзывы: список, статистика, создание/обновление/удаление по userId")
@RestController
@RequestMapping("/api/products/{productId}/reviews")
@Validated
public class ReviewApiController {

    private final ReviewRepository reviews;
    private final ProductRepository products;

    public ReviewApiController(ReviewRepository reviews,
                               ProductRepository products) {
        this.reviews = reviews;
        this.products = products;
    }

    // ===== LIST =====
    @Operation(summary = "Список отзывов по товару")
    @GetMapping
    public List<ReviewDto> list(@PathVariable Integer productId) {
        return reviews.findAllByProduct_Id(productId).stream()
                .map(ReviewDto::from)
                .toList();
    }

    // ===== STATS =====
    @Operation(summary = "Статистика отзывов по товару (count, avg)")
    @GetMapping("/stats")
    public Stats stats(@PathVariable Integer productId) {
        var list = reviews.findAllByProduct_Id(productId);
        int count = list.size();
        double avg = count == 0 ? 0.0 : list.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return new Stats(count, avg);
    }

    // ===== CREATE (one-per-user) =====
    @Operation(summary = "Оставить отзыв от конкретного пользователя (userId в URL). У пользователя может быть только один отзыв на товар.")
    @PostMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<?> create(@PathVariable Integer productId,
                                    @PathVariable Integer userId,
                                    @Valid @RequestBody UpsertBody body) {
        if (reviews.existsByUser_UserIdAndProduct_Id(userId, productId)) {
            return ResponseEntity.status(409).body("Отзыв уже существует. Используйте PUT для обновления.");
        }

        Product p = products.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден"));

        // создаём «ссылку» на пользователя по id без отдельного запроса
        User u = new User();
        u.setUserId(userId);

        Review r = new Review();
        r.setProduct(p);
        r.setUser(u);
        r.setRating(body.rating());
        r.setComment(body.comment());
        r.setCreatedAt(OffsetDateTime.now());

        var saved = reviews.save(r);
        return ResponseEntity.ok(ReviewDto.from(saved));
    }

    // ===== UPDATE (only that user's review) =====
    @Operation(summary = "Обновить отзыв конкретного пользователя по товару")
    @PutMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Integer productId,
                                    @PathVariable Integer userId,
                                    @Valid @RequestBody UpsertBody body) {
        Review r = reviews.findByUser_UserIdAndProduct_Id(userId, productId)
                .orElseThrow(() -> new NoSuchElementException("Отзыв пользователя не найден"));
        r.setRating(body.rating());
        r.setComment(body.comment());
        var saved = reviews.save(r);
        return ResponseEntity.ok(ReviewDto.from(saved));
    }

    // ===== DELETE (only that user's review) =====
    @Operation(summary = "Удалить отзыв конкретного пользователя по товару")
    @DeleteMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Integer productId,
                                    @PathVariable Integer userId) {
        if (!reviews.existsByUser_UserIdAndProduct_Id(userId, productId)) {
            return ResponseEntity.notFound().build();
        }
        reviews.deleteByUser_UserIdAndProduct_Id(userId, productId);
        return ResponseEntity.noContent().build();
    }

    // ===== DTOs =====
    public record UpsertBody(
            @Min(1) @Max(5) Integer rating,
            @Size(max = 300) String comment
    ) {}

    public record ReviewDto(
            Integer reviewId,
            String userLogin,
            Integer rating,
            String comment,
            OffsetDateTime createdAt
    ) {
        public static ReviewDto from(Review r) {
            return new ReviewDto(
                    r.getReviewId(),
                    r.getUser() != null ? r.getUser().getLogin() : null,
                    r.getRating(),
                    r.getComment(),
                    r.getCreatedAt()
            );
        }
    }

    public record Stats(int count, double avg) {}
}
