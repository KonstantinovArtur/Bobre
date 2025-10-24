package ru.forge.blacksmith_shop.catalog.domain.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "vw_products_catalog")
@Immutable                 // это VIEW — только чтение
public class ProductCatalogView {

    @Id
    @Column(name = "product_id")
    private Integer productId;

    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "category_name")
    private String categoryName;

    private BigDecimal price;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;

    @Column(name = "price_final")
    private BigDecimal priceFinal;

    @Column(name = "stock_qty")
    private Integer stockQty;

    @Column(name = "is_promotional")
    private Boolean isPromotional;

    @Column(name = "avg_rating")
    private BigDecimal avgRating;

    @Column(name = "reviews_count")
    private Integer reviewsCount;

    @Column(name = "primary_image_id")
    private Integer primaryImageId;

    @Column(name = "primary_image_filename")
    private String primaryImageFilename;

    @Column(name = "primary_image_mime")
    private String primaryImageMime;

    // --- геттеры (можно сгенерировать Lombok'ом)
    public Integer getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getPriceFinal() { return priceFinal; }
    public Integer getStockQty() { return stockQty; }
    public Boolean getIsPromotional() { return isPromotional; }
    public BigDecimal getAvgRating() { return avgRating; }
    public Integer getReviewsCount() { return reviewsCount; }
    public Integer getPrimaryImageId() { return primaryImageId; }
    public String getPrimaryImageFilename() { return primaryImageFilename; }
    public String getPrimaryImageMime() { return primaryImageMime; }
}
