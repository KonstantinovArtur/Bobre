package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ru.forge.blacksmith_shop.catalog.domain.ProductImage;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    @Query(value = """
      SELECT pi.* FROM product_images pi
      WHERE pi.product_id = :productId
      ORDER BY pi.is_primary DESC, pi.image_id ASC
      LIMIT 1
      """, nativeQuery = true)
    Optional<ProductImage> findPrimaryByProductId(@Param("productId") Integer productId);
}
