package ru.forge.blacksmith_shop.catalog.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import ru.forge.blacksmith_shop.users.domain.User;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ru.forge.blacksmith_shop.catalog.domain.Product product;

    private Integer rating;

    @Column(length = 2000)
    @Size(max = 300, message = "Комментарий не длиннее 300 символов")
    private String comment;

    private OffsetDateTime createdAt;

    public Integer getReviewId() { return reviewId; }
    public ru.forge.blacksmith_shop.users.domain.User getUser() { return user; }
    public ru.forge.blacksmith_shop.catalog.domain.Product getProduct() { return product; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setUser(ru.forge.blacksmith_shop.users.domain.User user) { this.user = user; }
    public void setProduct(ru.forge.blacksmith_shop.catalog.domain.Product product) { this.product = product; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
