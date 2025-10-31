// src/main/java/ru/forge/blacksmith_shop/catalog/repo/ProductImageRepository.java
package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.catalog.domain.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    // Сбросить текущее "главное" изображение товара
    @Modifying
    @Transactional
    @Query("update ProductImage pi set pi.primary = false where pi.product.id = :productId and pi.primary = true")
    void resetPrimary(@Param("productId") Integer productId);

    // Получить главное изображение товара (native — соответствует твоей БД)
    @Query(value = """
        SELECT pi.* FROM product_images pi
        WHERE pi.product_id = :productId
        ORDER BY pi.is_primary DESC, pi.image_id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProductImage> findPrimaryByProductId(@Param("productId") Integer productId);

    // Полезно для отладки/галереи, если пригодится
    @Query("select pi from ProductImage pi where pi.product.id = :productId order by pi.primary desc, pi.id asc")
    List<ProductImage> findAllByProductId(@Param("productId") Integer productId);
}
