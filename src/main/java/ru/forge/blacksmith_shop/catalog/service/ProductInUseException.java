package ru.forge.blacksmith_shop.catalog.service;

public class ProductInUseException extends RuntimeException {
    private final long count;
    public ProductInUseException(String reason, long count) {
        super("Нельзя удалить товар: " + reason + " (" + count + ")");
        this.count = count;
    }
    public long getCount() { return count; }
}
