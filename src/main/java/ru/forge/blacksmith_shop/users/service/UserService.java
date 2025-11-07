// src/main/java/ru/forge/blacksmith_shop/users/service/UserService.java
package ru.forge.blacksmith_shop.users.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import ru.forge.blacksmith_shop.order.repo.OrderAdminRepository;
import ru.forge.blacksmith_shop.users.dto.UserRow;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final OrderAdminRepository orders;   // ← добавили

    public UserService(UserRepository users,
                       OrderAdminRepository orders) {   // ← добавили параметр
        this.users = users;
        this.orders = orders;
    }

    public List<UserRow> list() {
        return users.findAllRows();
    }

    /** Удаление с проверкой: если есть заказы — бросаем исключение */
    public void deleteById(Integer id) {
        if (!users.existsById(id)) {
            throw new EntityNotFoundException("Пользователь не найден: id=" + id);
        }
        long cnt = orders.countByUserId(id);
        if (cnt > 0) {
            throw new UserInUseException(cnt);
        }
        users.deleteById(id);
    }
}
