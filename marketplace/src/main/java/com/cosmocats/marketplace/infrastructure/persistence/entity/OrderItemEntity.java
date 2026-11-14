package com.cosmocats.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
        uniqueConstraints = @UniqueConstraint(name = "uq_order_items_order_product",
                columnNames = {"order_id", "product_id"}))
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq_gen")
    @SequenceGenerator(name = "order_items_seq_gen", sequenceName = "order_items_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private Integer quantity;

    /** Ціна товару на момент покупки. */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItemEntity() {
    }

    public OrderItemEntity(ProductEntity product, Integer quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() { return id; }

    public OrderEntity getPurchaseOrder() { return purchaseOrder; }

    void setPurchaseOrder(OrderEntity purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public ProductEntity getProduct() { return product; }

    public Integer getQuantity() { return quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
}
