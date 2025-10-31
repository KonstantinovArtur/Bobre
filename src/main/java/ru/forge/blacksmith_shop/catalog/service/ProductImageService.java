// src/main/java/ru/forge/blacksmith_shop/catalog/service/ProductImageService.java
package ru.forge.blacksmith_shop.catalog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.domain.ProductImage;
import ru.forge.blacksmith_shop.catalog.repo.ProductImageRepository;

@Service
public class ProductImageService {

    private final ProductImageRepository images;

    public ProductImageService(ProductImageRepository images) {
        this.images = images;
    }

    @Transactional
    public void savePrimaryImage(Product product, MultipartFile file) {
        if (file == null || file.isEmpty()) return;

        // делаем новый primary
        images.resetPrimary(product.getId());

        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
        img.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        try {
            img.setBytes(file.getBytes());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Не удалось прочитать файл картинки", e);
        }
        img.setPrimary(true);
        images.save(img);
    }
}
