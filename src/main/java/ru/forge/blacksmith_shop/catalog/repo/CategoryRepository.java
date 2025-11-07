package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.forge.blacksmith_shop.catalog.domain.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Category> findByNameIgnoreCase(String name);


}
