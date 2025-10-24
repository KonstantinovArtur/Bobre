package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.forge.blacksmith_shop.catalog.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {}
