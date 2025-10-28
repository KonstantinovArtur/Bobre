package ru.forge.blacksmith_shop.catalog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "is_promotional", nullable = false)
    private boolean promotional;

    /** совместимость с шаблонами и CartService — возвращает ID товара */
    public Integer getProductId() {
        return id;
    }

    /** ручной геттер для имени — обход проблем Lombok при компиляции */
    public String getName() {
        return this.name;
    }

    /** финальная цена с учётом скидки (если скидки нет — возвращает price) */
    public BigDecimal getPriceFinal() {
        if (price == null) return BigDecimal.ZERO;
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        BigDecimal discount = price
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return price.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    public String getCategoryName() {
        if (category == null) return null;

        // Если в Category есть getName()
        try {
            return (String) category.getClass().getMethod("getName").invoke(category);
        } catch (Exception ignored) {}

        // Если в Category есть getCategoryName()
        try {
            return (String) category.getClass().getMethod("getCategoryName").invoke(category);
        } catch (Exception ignored) {}

        // Ни один геттер не найден
        return null;
    }


    /** если нужно получить скидку как int (удобно для отображения) */
    public Integer getDiscountPercentInt() {
        return discountPercent != null ? discountPercent.setScale(0, RoundingMode.DOWN).intValue() : 0;
    }

    /** совместимость с выражениями item.isPromotional в шаблонах */
    public boolean isPromotional() {
        return promotional;
    }
}
