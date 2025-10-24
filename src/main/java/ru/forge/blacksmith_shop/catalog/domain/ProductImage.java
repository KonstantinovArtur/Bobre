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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "bytes", columnDefinition = "bytea", nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private byte[] bytes;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryImage;

    public String getMimeType() { return mimeType; }
    public byte[] getBytes() { return bytes; }
}



