package ru.forge.blacksmith_shop.catalog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "products")
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

    public Product() {
    }

    // --------- getters/setters ----------
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    // alias used in some places
    public Integer getProductId() { return id; }
    public void setProductId(Integer id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    // boolean with both is/get for full compatibility
    public boolean isPromotional() { return promotional; }
    public boolean getPromotional() { return promotional; } // некоторые контроллеры вызывают get*
    public void setPromotional(boolean promotional) { this.promotional = promotional; }

    // --------- helpers (как было) ----------
    /** Финальная цена с учётом скидки */
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

    /** Удобный доступ к названию категории (бережно к LAZY-прокси) */
    public String getCategoryName() {
        if (category == null) return null;
        try { return category.getName(); } catch (Exception ignored) {}
        // бэкап через reflection (если меняли модель)
        try { return (String) category.getClass().getMethod("getName").invoke(category); }
        catch (Exception ignored) { return null; }
    }

    /** Скидка как целое число (для UI) */
    public Integer getDiscountPercentInt() {
        return discountPercent != null
                ? discountPercent.setScale(0, RoundingMode.DOWN).intValue()
                : 0;
    }
}
