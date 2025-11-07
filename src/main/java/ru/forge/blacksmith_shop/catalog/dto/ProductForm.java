// src/main/java/ru/forge/blacksmith_shop/catalog/dto/ProductForm.java
package ru.forge.blacksmith_shop.catalog.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductForm {
    private Integer id;

    @NotBlank(message = "Название обязательно")
    @Size(min = 3, max = 30, message = "Длина названия: от 3 до 30 символов")
    @Pattern(regexp = "^[\\p{L}].*$", message = "Название должно начинаться с буквы")
    private String name;

    @NotBlank(message = "Описание обязательно")
    @Size(min = 3, max = 200, message = "Длина описания: от 3 до 200 символов")
    @Pattern(regexp = "^[\\p{L}].*$", message = "Описание должно начинаться с буквы")
    private String description;

    private BigDecimal price;
    private Integer stockQty;
    private BigDecimal discountPercent;
    private Boolean promotional;
    private Integer categoryId;

    // getters/setters...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name){ this.name = name; }

    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }

    public BigDecimal getPrice(){ return price; }
    public void setPrice(BigDecimal price){ this.price = price; }

    public Integer getStockQty(){ return stockQty; }
    public void setStockQty(Integer stockQty){ this.stockQty = stockQty; }

    public BigDecimal getDiscountPercent(){ return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent){ this.discountPercent = discountPercent; }

    public Boolean getPromotional(){ return promotional; }
    public void setPromotional(Boolean promotional){ this.promotional = promotional; }

    public Integer getCategoryId(){ return categoryId; }
    public void setCategoryId(Integer categoryId){ this.categoryId = categoryId; }
}
