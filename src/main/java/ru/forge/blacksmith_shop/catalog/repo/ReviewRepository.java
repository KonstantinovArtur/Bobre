package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.forge.blacksmith_shop.catalog.domain.Review;
import ru.forge.blacksmith_shop.catalog.dto.ReviewDto;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // JPQL-проекция сразу в DTO
    @Query("""
  select new ru.forge.blacksmith_shop.catalog.dto.ReviewDto(
    r.reviewId, u.login, r.rating, r.comment, r.createdAt
  )
  from Review r
    join r.user u
    join r.product p
  where p.id = :productId         
  order by r.createdAt desc, r.reviewId desc
""")
    List<ReviewDto> findAllByProductId(@Param("productId") Integer productId);
}
