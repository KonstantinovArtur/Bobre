package ru.forge.blacksmith_shop.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forge.blacksmith_shop.cart.Cart;
import ru.forge.blacksmith_shop.cart.CartService;

import java.math.BigDecimal;

@Service
public class OrderService {
    private final OrderDao orders;
    private final CartService cartService;

    public OrderService(OrderDao orders, CartService cartService) {
        this.orders = orders;
        this.cartService = cartService;
    }

    @Transactional
    public OrderDao.CreatedOrder checkout(Cart sessionCart, int userId, String address, BigDecimal shipping) {
        // создаём заказ в БД (функция сама спишет склад и очистит таблицу cart_items)
        var created = orders.createFromCart(userId, address == null ? "" : address, shipping);
        // чистим сессионную корзину, чтобы UI сразу стал пустым
        cartService.clearCart(sessionCart);
        return created;
    }
}
