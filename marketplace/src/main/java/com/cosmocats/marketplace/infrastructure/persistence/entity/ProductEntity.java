package com.cosmocats.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uq_products_category_name",
                columnNames = {"category_id", "name"}))
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProductEntity() {
    }

    public ProductEntity(String name, String description, BigDecimal price,
                         CategoryEntity category, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }

    public void setPrice(BigDecimal price) { this.price = price; }

    public CategoryEntity getCategory() { return category; }

    public void setCategory(CategoryEntity category) { this.category = category; }

    public Integer getStockQuantity() { return stockQuantity; }

    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Instant getCreatedAt() { return createdAt; }
}
