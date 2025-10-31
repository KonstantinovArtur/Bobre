// src/main/java/ru/forge/blacksmith_shop/manager/dto/ProductForm.java
package ru.forge.blacksmith_shop.catalog.dto;

import java.math.BigDecimal;

public class ProductForm {
    private Integer id;                 // для редактирования
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private BigDecimal discountPercent;
    private Boolean promotional;
    private Integer categoryId;

    // getters/setters
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
