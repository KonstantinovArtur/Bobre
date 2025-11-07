// src/main/java/ru/forge/blacksmith_shop/catalog/dto/CategoryForm.java
package ru.forge.blacksmith_shop.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryForm {

    private Integer id;

    @NotBlank(message = "Укажите название категории")
    @Size(min = 3, max = 25, message = "Название: от 3 до 25 символов")
    @Pattern(
            regexp = "^(?=\\p{L})[\\p{L}\\p{M}0-9 \\-]{3,25}$",
            message = "Название должно начинаться с буквы и содержать только буквы, цифры, пробелы и дефисы"
    )
    private String name;

    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
}
