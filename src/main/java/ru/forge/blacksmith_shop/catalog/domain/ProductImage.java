// src/main/java/ru/forge/blacksmith_shop/catalog/domain/ProductImage.java
package ru.forge.blacksmith_shop.catalog.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    // ВАЖНО: byte[] + columnDefinition=bytea + JdbcTypeCode(BINARY) => PG bytea
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "bytes", nullable = false, columnDefinition = "bytea")
    @JdbcTypeCode(SqlTypes.BINARY)
    private byte[] bytes;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    // --- getters/setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) { this.bytes = bytes; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
