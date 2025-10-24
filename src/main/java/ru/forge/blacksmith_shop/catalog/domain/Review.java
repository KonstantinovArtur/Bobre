package ru.forge.blacksmith_shop.catalog.domain;

import jakarta.persistence.*;
import ru.forge.blacksmith_shop.users.domain.User;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer rating;
    private String comment;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    // геттеры/сеттеры
}
