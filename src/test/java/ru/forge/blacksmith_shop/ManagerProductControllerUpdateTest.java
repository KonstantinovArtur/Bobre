package ru.forge.blacksmith_shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.forge.blacksmith_shop.catalog.domain.Category;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.domain.ProductImage;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManagerProductController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ManagerProductControllerUpdateTest {

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

    // ---------- 1. УСПЕШНОЕ ИЗМЕНЕНИЕ ----------
    @Test
    void updateProduct_Success() throws Exception {
        Integer id = 10;

        Category oldCat = new Category();
        oldCat.setId(1);
        oldCat.setName("Старая категория");

        Product existing = new Product();
        existing.setId(id);
        existing.setName("Старое имя");
        existing.setDescription("Старое описание");
        existing.setPrice(BigDecimal.valueOf(10));
        existing.setStockQty(1);
        existing.setDiscountPercent(BigDecimal.ZERO);
        existing.setPromotional(false);
        existing.setCategory(oldCat);

        when(products.findById(id)).thenReturn(Optional.of(existing));

        Category newCat = new Category();
        newCat.setId(2);
        newCat.setName("Новая категория");
        when(categories.findById(2)).thenReturn(Optional.of(newCat));

        when(products.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(
                        post("/manager/products/{id}/update", id)
                                .param("name", "Новое имя")
                                .param("description", "Новое описание")
                                .param("price", "99.99")
                                .param("stockQty", "10")
                                .param("discountPercent", "5")
                                .param("promotional", "true")
                                .param("categoryId", "2")
                )
                .andExpect(status().is3xxRedirection())
                // строка ok URL-кодируется, поэтому проверяем только шаблон
                .andExpect(redirectedUrlPattern("/manager/products?ok=*"));

        // проверяем, что сущность была изменена и сохранена
        verify(products).save(argThat(p ->
                p.getId().equals(id)
                        && p.getName().equals("Новое имя")
                        && p.getDescription().equals("Новое описание")
                        && p.getPrice().compareTo(new BigDecimal("99.99")) == 0
                        && p.getStockQty().equals(10)
                        && p.getDiscountPercent().compareTo(new BigDecimal("5")) == 0
                        && p.isPromotional()
                        && p.getCategory() == newCat
        ));
    }

    // ---------- 2. ОШИБКА ВАЛИДАЦИИ ----------
    @Test
    void updateProduct_ValidationFail_ReturnsForm() throws Exception {
        Integer id = 11;

        Product existing = new Product();
        existing.setId(id);
        when(products.findById(id)).thenReturn(Optional.of(existing));

        // изображения и категории нужны для заполнения модели при ошибках
        ProductImage img = new ProductImage();
        img.setId(1);
        when(images.findAllByProductIdOrder(id)).thenReturn(List.of(img));
        when(categories.findAll()).thenReturn(List.of());

        mockMvc.perform(
                        post("/manager/products/{id}/update", id)
                                .param("name", "") // некорректно: NotBlank + Size
                                .param("description", "Ок")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("manager/products/form"))
                .andExpect(model().attributeHasFieldErrors("form", "name"))
                .andExpect(model().attributeExists("categories", "images", "primaryImageId", "isEdit"));

        verify(products, never()).save(any());
    }

    // ---------- 3. ТОВАР НЕ НАЙДЕН ----------
    @Test
    void updateProduct_ProductNotFound_Redirects() throws Exception {
        Integer id = 999;
        when(products.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/manager/products/{id}/update", id)
                                .param("name", "Имя")
                                .param("description", "Описание")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/manager/products?ok=*"));

        verify(products, never()).save(any());
    }
}
