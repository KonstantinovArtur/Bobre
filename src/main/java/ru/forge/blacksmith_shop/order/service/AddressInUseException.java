package ru.forge.blacksmith_shop.order.service;

public class AddressInUseException extends RuntimeException {
    private final long count;

    public AddressInUseException(long count) {
        super("Адрес используется в " + count + " заказ(ах)");
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
