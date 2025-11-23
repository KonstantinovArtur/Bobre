package ru.forge.blacksmith_shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.domain.ProductImage;
import ru.forge.blacksmith_shop.catalog.dto.ProductForm;
import ru.forge.blacksmith_shop.catalog.repo.CategoryRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductImageRepository;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;
import ru.forge.blacksmith_shop.catalog.service.ProductService;
import ru.forge.blacksmith_shop.catalog.web.ManagerProductController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManagerProductController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ManagerProductControllerCreateTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductRepository products;

    @MockBean
    CategoryRepository categories;

    @MockBean
    ProductImageRepository images;

    @MockBean
    ProductService productService;

    // ---------- SUCCESS CREATE ----------
    @Test
    void createProduct_Success() throws Exception {
        // Категория существует
        Category cat = new Category();
        cat.setId(1);
        cat.setName("Категория 1");

        when(categories.findAll()).thenReturn(List.of(cat));
        when(categories.findById(1)).thenReturn(Optional.of(cat));

        // Сохраняем товар
        when(products.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10); // имитируем ID от базы
            return p;
        });

        MockMultipartFile upload = new MockMultipartFile(
                "upload",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(
                        multipart("/manager/products/create")
                                .file(upload)
                                .param("name", "Товар1")
                                .param("description", "Описание товара")
                                .param("price", "100.00")
                                .param("stockQty", "5")
                                .param("discountPercent", "0")
                                .param("promotional", "false")
                                .param("categoryId", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/manager/products?ok=*"));

        verify(products).save(any(Product.class));
        verify(images).save(any(ProductImage.class));
    }

    // ---------- VALIDATION FAIL ----------
    @Test
    void createProduct_ValidationFail_ReturnsForm() throws Exception {
        when(categories.findAll()).thenReturn(List.of());

        mockMvc.perform(
                        post("/manager/products/create")
                                .param("name", "") // пустое имя -> ошибка
                                .param("description", "Ок")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("manager/products/form"))
                .andExpect(model().attributeHasFieldErrors("form", "name"));
    }

    // ---------- NO UPLOAD ----------
    @Test
    void createProduct_NoUpload_StillSuccess() throws Exception {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("Категория 1");

        when(categories.findById(1)).thenReturn(Optional.of(cat));
        when(categories.findAll()).thenReturn(List.of(cat));

        when(products.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(20);
            return p;
        });

        mockMvc.perform(
                        post("/manager/products/create")
                                .param("name", "Товар2")
                                .param("description", "Описание")
                                .param("price", "50")
                                .param("stockQty", "0")
                                .param("discountPercent", "0")
                                .param("promotional", "false")
                                .param("categoryId", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/manager/products?ok=*"));

        verify(products).save(any(Product.class));
        verify(images, never()).save(any());
    }
}
