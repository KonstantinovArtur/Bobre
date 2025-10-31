// src/main/java/ru/forge/blacksmith_shop/manager/dto/CategoryForm.java
package ru.forge.blacksmith_shop.catalog.dto;

public class CategoryForm {
    private Integer id;
    private String name;

    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
}
