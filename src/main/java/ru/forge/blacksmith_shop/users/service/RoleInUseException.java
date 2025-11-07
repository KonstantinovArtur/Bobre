// src/main/java/ru/forge/blacksmith_shop/users/service/RoleInUseException.java
package ru.forge.blacksmith_shop.users.service;

public class RoleInUseException extends RuntimeException {
    private final long count;
    public RoleInUseException(long count) {
        super("Нельзя удалить роль: к ней привязано " + count + " пользовател(ей)");
        this.count = count;
    }
    public long getCount() { return count; }
}
