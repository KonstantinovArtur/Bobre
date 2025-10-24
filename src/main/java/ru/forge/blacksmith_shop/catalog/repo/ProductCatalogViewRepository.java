package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.forge.blacksmith_shop.catalog.domain.view.ProductCatalogView;

public interface ProductCatalogViewRepository
        extends JpaRepository<ProductCatalogView, Integer> {
}
