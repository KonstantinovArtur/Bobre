package ru.forge.blacksmith_shop.catalog.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    @Autowired
    public CategoryService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products  = products;
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!categories.existsById(id)) {
            throw new EntityNotFoundException("Категория не найдена: id=" + id);
        }

        long cnt = products.countByCategoryId(id); // метод добавлен в ProductRepository
        if (cnt > 0) {
            throw new CategoryInUseException(cnt);
        }

        categories.deleteById(id);
    }
}
