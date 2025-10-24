package ru.forge.blacksmith_shop.catalog.service;

import org.springframework.stereotype.Service;
import ru.forge.blacksmith_shop.catalog.dto.ReviewDto;
import ru.forge.blacksmith_shop.catalog.repo.ReviewRepository;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviews;

    public ReviewService(ReviewRepository reviews) {
        this.reviews = reviews; // ← инициализация final-поля
    }

    public List<ReviewDto> byProduct(Integer productId) {
        return reviews.findAllByProductId(productId);
    }
}
