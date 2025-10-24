package ru.forge.blacksmith_shop.catalog.web;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.catalog.repo.ProductImageRepository;

@Controller
@RequestMapping("/products")
public class ProductImageController {

    private final ProductImageRepository imageRepo;

    public ProductImageController(ProductImageRepository imageRepo) {
        this.imageRepo = imageRepo;
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> primaryImage(@PathVariable Integer id) {
        return imageRepo.findPrimaryByProductId(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getMimeType()))
                        .cacheControl(CacheControl.noCache())
                        .body(img.getBytes()))
                .orElse(ResponseEntity.notFound().build());
    }
}
