package ru.forge.blacksmith_shop.catalog.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductImageRepository;
import ru.forge.blacksmith_shop.catalog.repo.ReviewRepository;

@Service
public class ProductService {

    private final ProductRepository products;
    private final ReviewRepository reviews;
    private final ProductImageRepository images;

    @Autowired
    public ProductService(ProductRepository products,
                          ReviewRepository reviews,
                          ProductImageRepository images) {
        this.products = products;
        this.reviews = reviews;
        this.images = images;
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!products.existsById(id)) {
            throw new EntityNotFoundException("Товар не найден: id=" + id);
        }

        long r = reviews.countByProduct_Id(id);
        if (r > 0) {
            throw new ProductInUseException("есть связанные отзывы", r);
        }

        long img = images.countByProduct_Id(id);
        if (img > 0) {
            throw new ProductInUseException("есть связанные изображения", img);
        }

        // Если есть другие связи (order_items, cart_items, favourites) — добавь аналогичные проверки здесь.

        products.deleteById(id);
    }
}
