// src/main/java/ru/forge/blacksmith_shop/users/service/UserInUseException.java
package ru.forge.blacksmith_shop.users.service;

public class UserInUseException extends RuntimeException {
    private final long count;
    public UserInUseException(long count) {
        super("Нельзя удалить пользователя: к нему привязано " + count + " заказ(ов)");
        this.count = count;
    }
    public long getCount() { return count; }
}
