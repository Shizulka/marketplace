package com.cosmocats.marketplace.infrastructure.persistence.entity;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Natural ID: бізнес-ідентифікатор замовлення (незмінний, унікальний). */
    @NaturalId
    @Column(name = "order_number", nullable = false, updatable = false, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(String orderNumber, CustomerEntity customer) {
        this.orderNumber = orderNumber;
        this.customer = customer;
    }

    public void addItem(OrderItemEntity item) {
        item.setPurchaseOrder(this);
        items.add(item);
    }

    /** Загальна сума не зберігається в БД (похідне значення), а рахується з позицій. */
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() { return id; }

    public String getOrderNumber() { return orderNumber; }

    public CustomerEntity getCustomer() { return customer; }

    public OrderStatus getStatus() { return status; }

    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }

    public List<OrderItemEntity> getItems() { return items; }
}
