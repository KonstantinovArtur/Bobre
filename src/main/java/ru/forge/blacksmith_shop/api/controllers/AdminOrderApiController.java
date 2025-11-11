// src/main/java/ru/forge/blacksmith_shop/api/controllers/AdminOrderApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.order.repo.OrderAdminRepository;
import ru.forge.blacksmith_shop.order.web.OrderAdminRow;

import java.util.List;

@Tag(name = "Admin Orders", description = "Админ-API: все заказы и заказы конкретного пользователя")
@RestController
@RequestMapping("/api/admin/orders")
@Validated
public class AdminOrderApiController {

    private final OrderAdminRepository repo;

    public AdminOrderApiController(OrderAdminRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Список всех заказов")
    @GetMapping
    public List<OrderAdminRow> all() {
        return repo.findAll();
    }

    @Operation(summary = "Список заказов конкретного пользователя")
    @GetMapping("/user/{userId}")
    public List<OrderAdminRow> byUser(@PathVariable Integer userId) {
        return repo.findAllByUserId(userId);
    }

}
