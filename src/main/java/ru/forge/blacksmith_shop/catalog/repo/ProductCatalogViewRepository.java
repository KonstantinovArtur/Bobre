package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.forge.blacksmith_shop.catalog.domain.view.ProductCatalogView;

import java.util.List;

public interface ProductCatalogViewRepository extends JpaRepository<ProductCatalogView, Integer> {

    @Query(value = """
        SELECT *
        FROM vw_products_catalog v
        WHERE (:q IS NULL OR :q = '' OR v.name ILIKE CONCAT('%', :q, '%') OR v.description ILIKE CONCAT('%', :q, '%'))
          AND (:catId IS NULL OR v.category_id = :catId)
        ORDER BY v.name ASC
        """, nativeQuery = true)
    List<ProductCatalogView> search(@Param("q") String q,
                                    @Param("catId") Integer catId);

    // чтобы без лишних условий просто получить всё, но отсортированным:
    List<ProductCatalogView> findAllByOrderByNameAsc();
}
