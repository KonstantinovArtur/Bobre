// src/main/java/ru/forge/blacksmith_shop/catalog/service/CategoryInUseException.java
package ru.forge.blacksmith_shop.catalog.service;

public class CategoryInUseException extends RuntimeException {
    private final long count;

    public CategoryInUseException(long count) {
        super("Категория используется в " + count + " товар(ах)");
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
