package com.cosmocats.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers",
        uniqueConstraints = @UniqueConstraint(name = "uq_customers_email", columnNames = "email"))
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerEntity() {
    }

    public CustomerEntity(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public UUID getId() { return id; }

    public String getEmail() { return email; }

    public String getFullName() { return fullName; }

    public Instant getCreatedAt() { return createdAt; }
}
