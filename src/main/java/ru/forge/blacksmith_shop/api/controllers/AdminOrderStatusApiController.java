// src/main/java/ru/forge/blacksmith_shop/api/controllers/AdminOrderStatusApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.order.repo.OrderAdminRepository;

@Tag(name = "Admin Orders", description = "Изменение статуса заказа (валидные переходы)")
@RestController
@RequestMapping("/api/admin/orders")
@Validated
public class AdminOrderStatusApiController {

    private final OrderAdminRepository repo;

    public AdminOrderStatusApiController(OrderAdminRepository repo) {
        this.repo = repo;
    }

    // created -> in_transit
    @Operation(summary = "Перевести заказ в статус 'in_transit' (только из 'created')")
    @PutMapping("/{orderId}/status/in_transit")
    public ResponseEntity<?> toInTransit(@PathVariable int orderId) {
        int updated = repo.markInTransitIfCreated(orderId);
        if (updated == 1) return ResponseEntity.noContent().build();
        if (!repo.existsById(orderId)) return ResponseEntity.notFound().build();
        return ResponseEntity.status(409).body("Нельзя перевести: текущий статус не 'created'");
    }

    // in_transit -> received
    @Operation(summary = "Перевести заказ в статус 'received' (только из 'in_transit')")
    @PutMapping("/{orderId}/status/received")
    public ResponseEntity<?> toReceived(@PathVariable int orderId) {
        int updated = repo.markReceivedIfInTransit(orderId);
        if (updated == 1) return ResponseEntity.noContent().build();
        if (!repo.existsById(orderId)) return ResponseEntity.notFound().build();
        return ResponseEntity.status(409).body("Нельзя перевести: текущий статус не 'in_transit'");
    }
}
