package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.forge.blacksmith_shop.catalog.domain.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // все отзывы по товару
    List<Review> findAllByProduct_Id(Integer productId);

    // один отзыв пользователя для конкретного товара
    Optional<Review> findByUser_UserIdAndProduct_Id(Integer userId, Integer productId);

    // проверка — есть ли отзыв
    boolean existsByUser_UserIdAndProduct_Id(Integer userId, Integer productId);

    // удалить отзыв
    void deleteByUser_UserIdAndProduct_Id(Integer userId, Integer productId);
}
