// src/main/java/ru/forge/blacksmith_shop/api/controllers/AdminCartApiController.java
package ru.forge.blacksmith_shop.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.cart.db.CartDbItem;
import ru.forge.blacksmith_shop.cart.db.CartRepository;
import ru.forge.blacksmith_shop.catalog.domain.Product;
import ru.forge.blacksmith_shop.catalog.repo.ProductRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Admin Cart", description = "Управление корзиной конкретного пользователя (по userId)")
@RestController
@RequestMapping("/api/admin/carts")
@Validated
public class CartApiController {

    private final CartRepository cartRepo;
    private final ProductRepository products;

    public CartApiController(CartRepository cartRepo, ProductRepository products) {
        this.cartRepo = cartRepo;
        this.products = products;
    }

    // ===== VIEW =====
    @Operation(summary = "Получить корзину пользователя")
    @GetMapping("/{userId}")
    public CartView getCart(@PathVariable int userId) {
        var rows = cartRepo.findByUserId(userId);
        if (rows.isEmpty()) return new CartView(List.of(), BigDecimal.ZERO);

        Map<Integer, Integer> qtyByPid = rows.stream()
                .collect(Collectors.toMap(CartDbItem::getProductId, CartDbItem::getQuantity));

        var productMap = products.findAllById(qtyByPid.keySet()).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<CartLine> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var e : qtyByPid.entrySet()) {
            Product p = productMap.get(e.getKey());
            if (p == null) continue;
            int qty = e.getValue();
            BigDecimal price = p.getPriceFinal();
            BigDecimal line = price.multiply(BigDecimal.valueOf(qty));
            total = total.add(line);
            items.add(new CartLine(p.getProductId(), p.getName(), price, qty, line));
        }
        return new CartView(items, total);
    }

    // ===== ADD =====
    @Operation(summary = "Добавить товар в корзину пользователя (qty ≥ 1)")
    @PostMapping("/{userId}/add")
    @Transactional
    public ResponseEntity<?> add(@PathVariable int userId, @Valid @RequestBody AddBody body) {
        int qty = Math.max(1, body.qty());
        Product p = products.findById(body.productId())
                .orElseThrow(() -> new NoSuchElementException("Товар не найден"));

        int ok = products.tryReserveStock(p.getProductId(), qty);
        if (ok == 0) return ResponseEntity.badRequest().body("Недостаточно товара на складе");

        cartRepo.addOrIncrement(userId, p.getProductId(), qty);
        return ResponseEntity.noContent().build();
    }

    // ===== SET QTY =====
    @Operation(summary = "Установить точное количество по товару в корзине пользователя")
    @PutMapping("/{userId}/{productId}")
    @Transactional
    public ResponseEntity<?> setQty(@PathVariable int userId,
                                    @PathVariable int productId,
                                    @Valid @RequestBody SetQtyBody body) {
        int newQty = Math.max(0, body.qty());

        var rowOpt = cartRepo.findByUserIdAndProductId(userId, productId);
        int oldQty = rowOpt.map(CartDbItem::getQuantity).orElse(0);

        if (oldQty == newQty) return ResponseEntity.noContent().build();

        if (newQty == 0) {
            // удаляем и освобождаем весь резерв
            if (oldQty > 0) products.releaseStock(productId, oldQty);
            cartRepo.deleteLine(userId, productId);
            return ResponseEntity.noContent().build();
        }

        // увеличиваем?
        if (newQty > oldQty) {
            int delta = newQty - oldQty;
            // проверим, что товар существует
            products.findById(productId).orElseThrow(() -> new NoSuchElementException("Товар не найден"));
            int ok = products.tryReserveStock(productId, delta);
            if (ok == 0) return ResponseEntity.badRequest().body("Недостаточно товара на складе");

            CartDbItem row = rowOpt.orElseGet(() -> {
                var it = new CartDbItem();
                it.setUserId(userId);
                it.setProductId(productId);
                it.setQuantity(0);
                return it;
            });
            row.setQuantity(newQty);
            cartRepo.saveAndFlush(row);
            return ResponseEntity.noContent().build();
        }

        // уменьшаем
        int delta = oldQty - newQty;
        CartDbItem row = rowOpt.orElseThrow(() -> new NoSuchElementException("Строка корзины не найдена"));
        row.setQuantity(newQty);
        cartRepo.saveAndFlush(row);
        products.releaseStock(productId, delta);
        return ResponseEntity.noContent().build();
    }

    // ===== REMOVE LINE =====
    @Operation(summary = "Удалить товар из корзины пользователя")
    @DeleteMapping("/{userId}/{productId}")
    @Transactional
    public ResponseEntity<?> remove(@PathVariable int userId, @PathVariable int productId) {
        var rowOpt = cartRepo.findByUserIdAndProductId(userId, productId);
        if (rowOpt.isEmpty()) return ResponseEntity.noContent().build();

        CartDbItem row = rowOpt.get();
        if (row.getQuantity() > 0) products.releaseStock(productId, row.getQuantity());
        cartRepo.deleteLine(userId, productId);
        return ResponseEntity.noContent().build();
    }

    // ===== CLEAR =====
    @Operation(summary = "Очистить корзину пользователя")
    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> clear(@PathVariable int userId) {
        var rows = cartRepo.findByUserId(userId);
        for (CartDbItem it : rows) {
            if (it.getQuantity() > 0) products.releaseStock(it.getProductId(), it.getQuantity());
        }
        cartRepo.clearByUser(userId);
        return ResponseEntity.noContent().build();
    }

    // DTOs
    public record AddBody(@NotNull Integer productId, @Min(1) int qty) {}
    public record SetQtyBody(@Min(0) int qty) {}
    public record CartLine(Integer productId, String name, BigDecimal unitPrice, int qty, BigDecimal lineTotal) {}
    public record CartView(java.util.List<CartLine> items, BigDecimal total) {}
}
