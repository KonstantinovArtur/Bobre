package ru.forge.blacksmith_shop.catalog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.catalog.domain.Review;
import ru.forge.blacksmith_shop.catalog.repo.ReviewRepository;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    public ReviewService(ReviewRepository reviewRepo,
                         UserRepository userRepo,
                         ProductRepository productRepo) {
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    /** Все отзывы по товару */
    public List<Map<String, Object>> byProduct(Integer productId) {
        var list = reviewRepo.findAllByProduct_Id(productId);
        return list.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userName", r.getUser().getName());
            m.put("rating", r.getRating());
            m.put("comment", r.getComment());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).toList();
    }


    /** Отзыв пользователя по товару */
    public Optional<Review> findUserReview(Integer userId, Integer productId) {
        return reviewRepo.findByUser_UserIdAndProduct_Id(userId, productId);
    }

    /** Создать или обновить отзыв */
    @Transactional
    public void createOrUpdate(Integer userId, Integer productId, int rating, String comment) {
        var user = userRepo.findById(userId).orElseThrow();
        var product = productRepo.findById(productId).orElseThrow();

        var existing = reviewRepo.findByUser_UserIdAndProduct_Id(userId, productId);
        Review review = existing.orElseGet(() -> {
            Review r = new Review();
            r.setUser(user);
            r.setProduct(product);
            r.setCreatedAt(OffsetDateTime.now());
            return r;
        });

        review.setRating(rating);
        review.setComment(comment);
        reviewRepo.save(review);
    }

    /** Удалить отзыв */
    @Transactional
    public void delete(Integer userId, Integer productId) {
        reviewRepo.deleteByUser_UserIdAndProduct_Id(userId, productId);
    }
}
